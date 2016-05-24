/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.intcloud.core.docker;

import com.google.common.base.Strings;
import io.fabric8.docker.client.Config;
import io.fabric8.docker.client.ConfigBuilder;
import io.fabric8.docker.client.DefaultDockerClient;
import io.fabric8.docker.client.DockerClient;
import io.fabric8.docker.dsl.EventListener;
import io.fabric8.docker.dsl.OutputHandle;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.intcloud.common.IntCloudException;
import org.wso2.appcloud.common.util.AppCloudUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class DockerOpClient {

    private static Log log = LogFactory.getLog(DockerOpClient.class);

    DockerClient dockerClient;
    OutputHandle handle;

    final CountDownLatch buildDone = new CountDownLatch(1);
    final CountDownLatch pushDone = new CountDownLatch(1);

    public DockerOpClient() {
        Config config = new ConfigBuilder()
                .withDockerUrl(DockerOpClientConstants.DEFAULT_DOCKER_URL)
                .build();
        dockerClient = new DefaultDockerClient(config);
    }

    public DockerOpClient(String uri) {
        Config config = new ConfigBuilder()
                .withDockerUrl(uri)
                .withConnectionTimeout(Integer.parseInt(AppCloudUtil.getPropertyValue(DockerOpClientConstants
                        .DOCKER_CONNECTION_TIMEOUT)))
                .withRequestTimeout(Integer.parseInt(AppCloudUtil.getPropertyValue(DockerOpClientConstants
                        .DOCKER_REQUEST_TIMEOUT)))
                .withImagePushTimeout(Integer.parseInt(AppCloudUtil.getPropertyValue(DockerOpClientConstants
                        .DOCKER_PUSH_TIMEOUT)))
                .withImageBuildTimeout(Integer.parseInt(AppCloudUtil.getPropertyValue(DockerOpClientConstants
                        .DOCKER_BUILLD_TIMEOUT)))
                .withImageSearchTimeout(Integer.parseInt(AppCloudUtil.getPropertyValue(DockerOpClientConstants
                        .DOCKER_SEARCH_TIMEOUT)))
                .build();
        dockerClient = new DefaultDockerClient(config);
    }

    public void createDockerFile(String runtimeId, String artifactName, String dockerFilePath,
            String dockerTemplateFilePath, String artifactUrl, String hostName) throws IOException, IntCloudException {

        List<String> dockerFileConfigs = new ArrayList<String>();
        if (Strings.isNullOrEmpty(artifactUrl)) {
            String dockerFileTemplatePath = DockerUtil
                    .getDockerFileTemplatePath(runtimeId, dockerTemplateFilePath, "default");
            String artifactNameWithoutExtension = artifactName.substring(0, artifactName.lastIndexOf("."));
            for (String line : FileUtils.readLines(new File(dockerFileTemplatePath))) {
                if (line.contains("ARTIFACT_NAME")) {
                    dockerFileConfigs.add(line.replace("ARTIFACT_NAME", artifactName));
                } else if (line.contains("ARTIFACT_DIR")) {
                    dockerFileConfigs.add(line.replace("ARTIFACT_DIR", artifactNameWithoutExtension));
                } else if (line.contains("HOST_NAME")) {
                    dockerFileConfigs.add(line.replace("HOST_NAME", hostName));
                } else {
                    dockerFileConfigs.add(line);
                }
            }
        } else {
            String dockerFileTemplatePath = DockerUtil
                    .getDockerFileTemplatePath(runtimeId, dockerTemplateFilePath, "url");
            String artifactNameWithoutExtension = artifactName.substring(0, artifactName.lastIndexOf("."));

            for (String line : FileUtils.readLines(new File(dockerFileTemplatePath))) {
                if (line.contains("ARTIFACT_NAME")) {
                    dockerFileConfigs.add(line.replace("ARTIFACT_NAME", artifactName));
                } else if (line.contains("ARTIFACT_URL")) {
                    dockerFileConfigs.add(line.replace("ARTIFACT_URL", artifactUrl));
                } else if (line.contains("HOST_NAME")) {
                    dockerFileConfigs.add(line.replace("HOST_NAME", hostName));
                } else {
                    dockerFileConfigs.add(line);
                }
            }
        }
        FileUtils.writeLines(new File(dockerFilePath), dockerFileConfigs);
    }

    public void createDockerFileForGitHub(String runtimeId, String gitRepoUrl, String gitRepoBranch,
            String dockerFilePath, String projectRoot, String dockerGitHubTemplateFilePath, String hostName)
            throws IOException, IntCloudException {

        String dockerFileTemplatePath = DockerUtil.getDockerFileTemplatePath(runtimeId, dockerGitHubTemplateFilePath, "github");
        List<String> dockerFileConfigs = new ArrayList<String>();
        for (String line : FileUtils.readLines(new File(dockerFileTemplatePath))) {
            if (line.contains("GIT_REPO_URL")) {
                line = line.replace("GIT_REPO_URL", gitRepoUrl);
            }
            if (line.contains("GIT_REPO_BRANCH")) {
                line = line.replace("GIT_REPO_BRANCH", gitRepoBranch);
            }
            if (line.contains("PROJECT_ROOT")) {
                line = line.replace("PROJECT_ROOT", projectRoot);
            }
            if (line.contains("HOST_NAME")) {
                line = line.replace("HOST_NAME", hostName);
            }
            dockerFileConfigs.add(line);
        }
        FileUtils.writeLines(new File(dockerFilePath), dockerFileConfigs);
    }

    public void buildDockerImage(String repoUrl, String imageName, String tag, String dockerFileUrl)
            throws InterruptedException, IOException, IntCloudException {

        String dockerImage = repoUrl + "/" + imageName + ":" + tag;
        final boolean[] dockerStatusCheck = new boolean[1];
	    dockerStatusCheck[0] = true;
        handle = dockerClient.image().build()
                .withRepositoryName(dockerImage)
                .withNoCache()
                .usingListener(new EventListener() {
                    @Override
                    public void onSuccess(String message) {
                        log.info("Build Success:" + message);
                        buildDone.countDown();
                    }

                    @Override
                    public void onError(String message) {
                        log.error("Build Failure:" + message);
                        buildDone.countDown();
                        dockerStatusCheck[0] = false;
                    }

                    @Override
                    public void onEvent(String event) {
                        log.info(event);
                    }
                })
                .fromFolder(dockerFileUrl);
        buildDone.await();
        handle.close();

        if (!dockerStatusCheck[0]) {
            log.error("Docker image building failed: " + imageName + " repo: " + repoUrl + " docker file: "
                    + dockerFileUrl + " tag: " + tag);
            throw new IntCloudException(
                    "Docker image building failed: " + imageName + " repo: " + repoUrl + " docker file: "
                            + dockerFileUrl + " tag: " + tag);
        }
    }

    public void pushDockerImage(String repoUrl, String imageName, String tag)
            throws InterruptedException, IOException, IntCloudException {

        final boolean[] dockerStatusCheck = new boolean[1];
	    dockerStatusCheck[0] = true;
        String dockerImageName = repoUrl + "/" + imageName;
        handle = dockerClient.image().withName(dockerImageName).push().usingListener(new EventListener() {
            @Override
            public void onSuccess(String message) {
                log.info("Push Success:" + message);
                pushDone.countDown();
            }

            @Override
            public void onError(String message) {
                log.error("Push Failure:" + message);
                pushDone.countDown();
                dockerStatusCheck[0] = false;
            }

            @Override
            public void onEvent(String event) {
                log.info(event);
            }
        }).withTag(tag).toRegistry();
        pushDone.await();
        handle.close();

        if (!dockerStatusCheck[0]) {
            log.error("Docker image push failed: " + imageName + " repo: " + repoUrl + " tag: " + tag);
            throw new IntCloudException(
                    "Docker image push failed: " + imageName + " repo: " + repoUrl + " tag: " + tag);
        }
    }

    public void clientClose() throws IOException {
        dockerClient.close();
    }
}
