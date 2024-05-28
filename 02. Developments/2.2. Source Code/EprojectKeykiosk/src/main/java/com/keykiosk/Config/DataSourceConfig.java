package com.keykiosk.Config;
import com.keykiosk.Util.FileUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.jdbc.DataSourceBuilder;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Configuration
public class DataSourceConfig {

    @Bean
    public DataSource getDataSource() {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        try {
            String dbUrl = new String(Files.readAllBytes(Paths.get(FileUtil.GetPath.getPath("/metadata/dbUrl.dat"))));
            dataSourceBuilder.url(dbUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return dataSourceBuilder.build();
    }
}