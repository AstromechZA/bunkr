package com.bunkr_beta;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by benmeier on 15/10/25.
 *
 * This class contains publically readable plaintext information for how to read and decrypt the archive.
 */
public class Descriptor
{
    private final EncryptionDescriptor encryption;
    private final CompressionDescriptor compression;

    @JsonCreator
    public Descriptor(
            @JsonProperty("encryption") EncryptionDescriptor encryption,
            @JsonProperty("compression") CompressionDescriptor compression
    )
    {
        this.encryption = encryption;
        this.compression = compression;
    }

    public static class EncryptionDescriptor
    {
        public final int pbkdf2Iterations;
        public final int aesKeyLength;
        public final byte[] pbkdf2Salt;

        @JsonCreator
        public EncryptionDescriptor(
                @JsonProperty("pbkdf2Iterations") int pbkdf2Iterations,
                @JsonProperty("aesKeyLength") int aesKeyLength,
                @JsonProperty("pbkdf2Salt") byte[] pbkdf2Salt
        )
        {
            if (pbkdf2Iterations < 4096)
                throw new IllegalArgumentException("pbkdf2Iterations must be at least 4096");

            if (aesKeyLength != 256)
                throw new IllegalArgumentException("aesKeyLength must be 256");

            this.pbkdf2Iterations = pbkdf2Iterations;
            this.aesKeyLength = aesKeyLength;
            this.pbkdf2Salt = pbkdf2Salt;
        }
    }

    public static class CompressionDescriptor
    {
        public final String algorithm;

        @JsonCreator
        public CompressionDescriptor(
                @JsonProperty("algorithm") String algorithm)
        {
            if (!algorithm.toUpperCase().equals("ZLIB"))
                throw new IllegalArgumentException("'ZLIB' is the only allowed compression algorithm");
            this.algorithm = algorithm;
        }
    }

    public CompressionDescriptor getCompression()
    {
        return compression;
    }

    public EncryptionDescriptor getEncryption()
    {
        return encryption;
    }
}
