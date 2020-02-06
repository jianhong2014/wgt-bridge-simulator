package com.orpak.fho;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@ComponentScan({"com.gvr.datahub.simulator"})
public class WgtBridgeSimulatorApp {
    public static void main(String[] args) {
        SpringApplication.run(com.orpak.fho.WgtBridgeSimulatorApp.class, args);
    }
}
