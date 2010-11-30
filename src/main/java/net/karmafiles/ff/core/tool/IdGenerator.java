package net.karmafiles.ff.core.tool;

import org.apache.commons.codec.binary.Base64;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicLong;

public class IdGenerator {

	private static AtomicLong atomicLong;

    private static SecureRandom mySecureRand;

    private static String s_id;

    static {

	    atomicLong = new AtomicLong();
        mySecureRand = new SecureRandom();
        try {
            s_id = InetAddress.getLocalHost().toString();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }             

    }

    private IdGenerator() {

    }

	private String getRandomGUID() {
        MessageDigest md5 = null;
        StringBuffer sbValueBeforeMD5 = new StringBuffer();

        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Error: " + e);
        }

        try {
            long time = System.currentTimeMillis();
            long rand = mySecureRand.nextLong();

	        // paranoid abs 
	        BigInteger bigInteger = new BigInteger("" + rand + Math.abs(time));

	        byte[] array = bigInteger.toByteArray();
	        return Base64.encodeBase64URLSafeString(array);

        } catch (Throwable t) {
            System.out.println("Error:" + t);
			throw new RuntimeException(t);	        
        }
    }
    
    public static String createSecureId() {
        return (new IdGenerator()).getRandomGUID();
    }

	public static String createNextSecureId() {
		IdGenerator idGenerator = new IdGenerator();
		return String.format("%019d-%s", atomicLong.incrementAndGet(), idGenerator.getRandomGUID());
	}
}