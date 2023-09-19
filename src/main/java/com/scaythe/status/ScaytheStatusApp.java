package com.scaythe.status;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;
import java.util.ServiceLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@Slf4j
public class ScaytheStatusApp {

  public static void main(String[] args) {
    SpringApplication.run(ScaytheStatusApp.class, args);
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

  public static Runnable logError(Runnable runnable) {
    return () -> {
      try {
        runnable.run();
      } catch (Exception e) {
        log.atError().setCause(e).log();
      }
    };
  }
}
