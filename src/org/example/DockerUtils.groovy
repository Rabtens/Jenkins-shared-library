package org.example

class DockerUtils {
    static void validateDockerfile(String path) {
        // You could add validation logic here
    }
    
    static String generateImageTag(String repo, String prefix = '') {
        def timestamp = new Date().format('yyyyMMddHHmmss')
        return "${repo}:${prefix}${timestamp}"
    }
}