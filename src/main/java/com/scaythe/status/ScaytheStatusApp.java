package com.scaythe.status;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.ServiceLoader;

@SpringBootApplication
public class ScaytheStatusApp {

    public static void main(String[] args) {
        try (ConfigurableApplicationContext ignored = SpringApplication.run(ScaytheStatusApp.class,
                args)) {
            try {
                Thread.currentThread().join();
            } catch (InterruptedException e) {
            }
        }
    }

    @Bean
    public Gson gson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        for (TypeAdapterFactory factory : ServiceLoader.load(TypeAdapterFactory.class)) {
            gsonBuilder.registerTypeAdapterFactory(factory);
        }

        gsonBuilder.disableHtmlEscaping();
//        gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);

        return gsonBuilder.create();
    }
}
