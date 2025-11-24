package org.openpodcastapi.opa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OpenPodcastAPI {

    static void main(String[] args) {
        SpringApplication.run(OpenPodcastAPI.class, args);
    }
}
