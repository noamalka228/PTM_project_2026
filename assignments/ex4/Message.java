package test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;

public class Message {
    public final byte[] data;
    public final String asText;
    public final Double asDouble;
    public final Date date;

    public Message(byte[] msg) {
        this(new String(msg, StandardCharsets.UTF_8));
    }

    public Message(String msg) {
        this(msg.getBytes(StandardCharsets.UTF_8), msg, toDoubleOrNaN(msg));
    }

    public Message(double msg) {
        this(Double.toString(msg));
    }

    private Message(byte[] data, String asText, double asDouble) {
        this.data = copy(data);
        this.asText = asText;
        this.asDouble = asDouble;
        this.date = new Date();
    }

    private static double toDoubleOrNaN(String text) {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

    private static byte[] copy(byte[] arr) {
        return Arrays.copyOf(arr, arr.length);
    }
}
