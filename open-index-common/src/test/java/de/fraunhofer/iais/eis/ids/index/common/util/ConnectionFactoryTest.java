package de.fraunhofer.iais.eis.ids.index.common.util;

import com.ginsberg.junit.exit.ExpectSystemExit;
import com.ginsberg.junit.exit.ExpectSystemExitWithStatus;
import com.github.jsonldjava.utils.Obj;
import org.junit.Test;

import static org.junit.Assert.*;

public class ConnectionFactoryTest {

    class DummyNoConnectionFactory extends ConnectionFactory {

        DummyNoConnectionFactory(String hostname, String port, Integer retries) {
            super(hostname, port, retries);
        }

        DummyNoConnectionFactory(String hostname, String port) {
            super(hostname, port);
        }

        DummyNoConnectionFactory() {
            super("", "");
        }

        @Override
        Object createConnection() throws Exception {
            throw new Exception();
        }


    }

    class DummyConnectionFactory extends ConnectionFactory {

        DummyConnectionFactory(String hostname, String port, Integer retries) {
            super(hostname, port, retries);
        }

        DummyConnectionFactory(String hostname, String port) {
            super(hostname, port);
        }

        DummyConnectionFactory() {
            super("", "");
        }

        @Override
        Object createConnection() throws Exception {
            return new String("test");
        }


    }

//    @Test
//    @ExpectSystemExitWithStatus(42)
//    public void getConnectionWithException() {
//        DummyNoConnectionFactory connectionFactory = new DummyNoConnectionFactory();
//        Object obj = connectionFactory.getConnection();
//    }

    @Test
    public void getConnectionWithoutException() {
        DummyConnectionFactory connectionFactory = new DummyConnectionFactory();
        String actual = (String) connectionFactory.getConnection();
        assertEquals(actual,"test");
    }
}