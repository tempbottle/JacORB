package org.jacorb.test.orb;

import java.util.Properties;

import junit.framework.*;

import org.jacorb.test.*;
import org.jacorb.test.common.*;

/**
 * Tests components of type TAG_ALTERNATE_IIOP_ADDRESS within IORs.
 * 
 * @author Andre Spiegel
 * @version $Id$
 */
public class AlternateIIOPAddressTest extends ClientServerTestCase
{
    protected IIOPAddressServer server = null;
    
    private static final String CORRECT_HOST = "127.0.0.1";
    private static final String WRONG_HOST   = "10.0.1.222";
    private static final String WRONG_HOST_2 = "10.0.1.223";
    
    private static final int CORRECT_PORT = 45000;
    private static final int WRONG_PORT   = 45001;

    public AlternateIIOPAddressTest(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    protected void setUp() throws Exception
    {
        server = IIOPAddressServerHelper.narrow(setup.getServerObject());
    }

    protected void tearDown() throws Exception
    {
        // server.clearSocketAddress();
        server.setIORAddress (CORRECT_HOST, CORRECT_PORT);
        server.clearAlternateAddresses();
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite("Test TAG_ALTERNATE_IIOP_ADDRESS");

        Properties client_props = new Properties();
        client_props.setProperty ("jacorb.retries", "2");
        client_props.setProperty ("jacorb.retry_interval", "50");

        Properties server_props = new Properties();
        server_props.setProperty 
            ("org.omg.PortableInterceptor.ORBInitializerClass."
           + "org.jacorb.test.orb.IIOPAddressORBInitializer", "");
        server_props.setProperty ("OAPort", Integer.toString(CORRECT_PORT));

        ClientServerSetup setup = 
        	new ClientServerSetup (suite,
                                   "org.jacorb.test.orb.IIOPAddressServerImpl",
                                   client_props,
                                   server_props);

        suite.addTest (new AlternateIIOPAddressTest("test_ping", setup));
        suite.addTest (new AlternateIIOPAddressTest("test_primary_ok", setup));
        suite.addTest (new AlternateIIOPAddressTest("test_primary_wrong_host", setup));
        suite.addTest (new AlternateIIOPAddressTest("test_primary_wrong_port", setup));
        suite.addTest (new AlternateIIOPAddressTest("test_alternate_ok", setup));
        suite.addTest (new AlternateIIOPAddressTest("test_alternate_ok_2", setup));
        suite.addTest (new AlternateIIOPAddressTest("test_alternate_wrong", setup));

        return setup;
    }
    
    public void test_ping()
    {
        Sample s = server.getObject();
        int result = s.ping (17);
        assertEquals (18, result);
    }

    public void test_primary_ok()
    {
        server.setIORAddress( CORRECT_HOST, CORRECT_PORT );
        Sample s = server.getObject();
        int result = s.ping (77);
        assertEquals (78, result);
    }
    
    public void test_primary_wrong_host()
    {
        server.setIORAddress( WRONG_HOST, CORRECT_PORT );
        Sample s = server.getObject();
        try
        {
            int result = s.ping (123);
            fail ("TRANSIENT exception expected");
        }
        catch (org.omg.CORBA.TRANSIENT ex)
        {
            // ok
        }
    }
    
    public void test_primary_wrong_port()
    {
        server.setIORAddress( CORRECT_HOST, WRONG_PORT );
        Sample s = server.getObject();
        try
        {
            int result = s.ping (4);
            fail ("TRANSIENT exception expected");
        }
        catch (org.omg.CORBA.TRANSIENT ex)
        {
            // ok
        }
    }
    
    public void test_alternate_ok()
    {
        server.setIORAddress( WRONG_HOST, CORRECT_PORT );
        server.addAlternateAddress( CORRECT_HOST, CORRECT_PORT );
        Sample s = server.getObject();
        int result = s.ping (99);
        assertEquals (100, result);
    }

    public void test_alternate_ok_2()
    {
        server.setIORAddress( WRONG_HOST, CORRECT_PORT );
        server.addAlternateAddress( WRONG_HOST_2, CORRECT_PORT );
        server.addAlternateAddress( CORRECT_HOST, CORRECT_PORT );
        Sample s = server.getObject();
        int result = s.ping (187);
        assertEquals (188, result);
    }

    public void test_alternate_wrong()
    {
        server.setIORAddress( CORRECT_HOST, WRONG_PORT );
        server.addAlternateAddress( WRONG_HOST, CORRECT_PORT );
        server.addAlternateAddress( WRONG_HOST_2, WRONG_PORT );
        server.addAlternateAddress( WRONG_HOST_2, WRONG_PORT );
        Sample s = server.getObject();
        try
        {
            int result = s.ping (33);
            fail ("TRANSIENT exception expected");
        }
        catch (org.omg.CORBA.TRANSIENT ex)
        {
            // ok
        }

    }

}
