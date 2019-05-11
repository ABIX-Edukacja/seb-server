/*
 * Copyright (c) 2019 ETH Zürich, Educational Development and Technology (LET)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ch.ethz.seb.sebserver.webservice.servicelayer.sebconfig.impl;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.cryptonode.jncryptor.AES256JNCryptor;
import org.cryptonode.jncryptor.AES256JNCryptorOutputStream;
import org.cryptonode.jncryptor.JNCryptor;
import org.junit.Test;

import ch.ethz.seb.sebserver.gbl.util.Result;
import ch.ethz.seb.sebserver.gbl.util.Utils;
import ch.ethz.seb.sebserver.webservice.servicelayer.sebconfig.SebConfigEncryptionContext;
import ch.ethz.seb.sebserver.webservice.servicelayer.sebconfig.SebConfigEncryptionService.Strategy;
import ch.ethz.seb.sebserver.webservice.servicelayer.sebconfig.impl.SebConfigEncryptionServiceImpl.EncryptionContext;

public class PasswordEncryptorTest {

    @Test
    public void conversionTest() throws IOException {
        final String text = "ojnjbiboijnlkncokdnvoiwjife";
        final byte[] byteArray = Utils.toByteArray(text);
        final byte[] otherByteArray = new byte[byteArray.length];
        final InputStream inputStream = IOUtils.toInputStream(text, "UTF-8");
        inputStream.read(otherByteArray);

        assertTrue(Arrays.equals(byteArray, otherByteArray));
    }

    @Test
    public void testUsingPassword() throws Exception {

        final String config = "<TestConfig></TestConfig>";
        final byte[] plaintext = Utils.toByteArray(config);//getRandomBytes(127);

        final String password = "Testing1234";

        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        final AES256JNCryptorOutputStream cryptorStream = new AES256JNCryptorOutputStream(
                byteStream, password.toCharArray());
        cryptorStream.write(plaintext);
        cryptorStream.close();

        final byte[] encrypted = byteStream.toByteArray();

        final JNCryptor cryptor = new AES256JNCryptor();

        final byte[] result = cryptor.decryptData(encrypted, password.toCharArray());
        assertArrayEquals(plaintext, result);
    }

    @Test
    public void test1() {
        final JNCryptor jnCryptor = new AES256JNCryptor();
        jnCryptor.setPBKDFIterations(10000);
        final PasswordEncryptor encryptor = new PasswordEncryptor(jnCryptor);

        final String config = "<TestConfig></TestConfig>";
        final String pwd = "password";

        final SebConfigEncryptionContext context = EncryptionContext.contextOf(
                Strategy.PASSWORD_PWCC,
                pwd);

        final Result<ByteBuffer> encrypt = encryptor.encrypt(config, context);
        assertFalse(encrypt.hasError());
        final ByteBuffer cipher = encrypt.getOrThrow();
        final byte[] byteArray = Utils.toByteArray(cipher);

        final Result<ByteBuffer> decrypt = encryptor.decrypt(cipher, context);
        assertFalse(decrypt.hasError());

        final String decryptedConfig = Utils.toString(decrypt.getOrThrow());
        assertEquals(config, decryptedConfig);
    }

    @Test
    public void test2() throws IOException {
        final JNCryptor jnCryptor = new AES256JNCryptor();
        jnCryptor.setPBKDFIterations(10000);
        final PasswordEncryptor encryptor = new PasswordEncryptor(jnCryptor);

        final String config = "<TestConfig></TestConfig>";
        final String pwd = "password";
        final ByteArrayOutputStream out = new ByteArrayOutputStream(512);

        final SebConfigEncryptionContext context = EncryptionContext.contextOf(
                Strategy.PASSWORD_PWCC,
                pwd);

        encryptor.encrypt(
                out,
                IOUtils.toInputStream(config, "UTF-8"),
                context);

        final byte[] byteArray = out.toByteArray();

        final Result<ByteBuffer> decrypt = encryptor.decrypt(
                ByteBuffer.wrap(byteArray),
                context);
        assertFalse(decrypt.hasError());

        final ByteBuffer buffer = decrypt.getOrThrow();
        buffer.rewind();
        final String decryptedConfig = Utils.toString(buffer);
        assertEquals(config, decryptedConfig);
    }

}
