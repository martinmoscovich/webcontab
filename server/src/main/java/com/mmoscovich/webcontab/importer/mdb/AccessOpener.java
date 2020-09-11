package com.mmoscovich.webcontab.importer.mdb;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;

import net.ucanaccess.jdbc.JackcessOpenerInterface;

/**
 * Clase necesaria para abrir los Access de WinContab con el encoding requerido.
 *
 */
public class AccessOpener implements JackcessOpenerInterface {

    @Override
    public Database open(File file, String string) throws IOException {
        DatabaseBuilder db = new DatabaseBuilder(file);

        db.setCharset(Charset.forName("Cp1252"));

        try {
            db.setReadOnly(false);
            return db.open();
        } catch (IOException e) {
            db.setReadOnly(true);
            return db.open();
        }
    }

}