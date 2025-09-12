package org.oosd.config;

import netscape.javascript.JSObject;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * This class will save/load JSON file to support user changes
 */


public final class ConfigService {
    public static final String fileName = "JavaTetrisConfig.json";
    public static final Path filePath = Path.of(System.getProperty("user.dir")).resolve(fileName);

    public static TetrisConfig currentConfig = TetrisConfig.defaults();

    public ConfigService()
    {

    }

    public static TetrisConfig get()
    {
        return currentConfig;
    }

    public static void load()
    {
        try
        {
            if(Files.exists(filePath))
            {
                String json = Files.readString(filePath, UTF_8);
               JSONObject object = new JSONObject(json);

                currentConfig = new TetrisConfig(
                        object.optInt("fieldWidth", 10),
                        object.optInt("fieldHeight", 20),
                        object.optInt("gameLevel", 5),
                        object.optBoolean("music", false),
                        object.optBoolean("sfx", false),
                        object.optBoolean("aiPlay", false),
                        object.optBoolean("extendMode", false)
                );
            }else {
                save(currentConfig);
            }
        }catch (Exception e)
        {
            e.printStackTrace();
            currentConfig = TetrisConfig.defaults();
        }
    }

    public static void save(TetrisConfig config)
    {
        try
        {
            JSONObject object = new JSONObject()
                    .put("fieldWidth", config.fieldWidth())
                    .put("fieldHeight", config.fieldHeight())
                    .put("gameLevel", config.gameLevel())
                    .put("music", config.music())
                    .put("sfx", config.sfx())
                    .put("aiPlay", config.aiPlay())
                    .put("extendMode", config.extendMode());
            Files.writeString(filePath, object.toString(2),
                    UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void update(TetrisConfig config)
    {
        save(config);
    }

}
