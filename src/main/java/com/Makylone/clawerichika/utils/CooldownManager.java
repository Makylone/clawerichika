package com.Makylone.clawerichika.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class CooldownManager {

    private static final String FILE_NAME = "command_cooldown_save.properties";
    private final String KEY_TIMESTAMP;

    public CooldownManager(String keyTimestampName) {
        this.KEY_TIMESTAMP = keyTimestampName;
    }

    public long loadLastExecution() {
        // Permet de lire plus tard la property sauvegardes dans le fichier .properties
        Properties properties = new Properties();
        // Lecture du fichier
        try (FileInputStream in = new FileInputStream(FILE_NAME)) {
            // Charge le contenu du fichier dans l'objet properties
            properties.load(in);
            String value_timestamp = properties.getProperty(this.KEY_TIMESTAMP);
            in.close();
            if (value_timestamp != null) {
                return Long.parseLong(value_timestamp);
            }
            // On peut avoir une erreur sur la lecture du ficher ou bien sur sa convertion en long
        } catch (IOException | NumberFormatException exception) {
            return 0L;
        }
        return 0L;
    }

    public void saveLastExecutionTime() {
        Properties properties = new Properties();
        properties.setProperty(
            this.KEY_TIMESTAMP,
            String.valueOf(System.currentTimeMillis())
        );
        try (FileOutputStream out = new FileOutputStream(FILE_NAME)) {
            properties.store(
                out,
                "Sauvegarde du timestamp de la dernière éxecution"
            );
            out.close();
        } catch (IOException eIoException) {
            eIoException.printStackTrace();
            System.err.println(
                "Impossible de sauvegarder le timestamps du cooldown"
            );
        }
    }
}
