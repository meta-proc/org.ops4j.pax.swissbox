/*
 * Copyright 2011 Harald Wellmann.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.swissbox.framework;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.base.exec.DefaultJavaRunner;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.FrameworkFactory;

public class RemoteFrameworkImplTest
{
    private DefaultJavaRunner javaRunner;
    private FrameworkFactory frameworkFactory;

    @Before
    public void setUp() throws RemoteException
    {
        LocateRegistry.createRegistry( 1099 );
        javaRunner = new DefaultJavaRunner( false );
        frameworkFactory = FrameworkFactoryFinder.loadSingleFrameworkFactory();
        String[] vmOptions = new String[]{
            "-Dosgi.console=6666",
            "-Dosgi.clean=true",
            "-Dorg.osgi.framework.storage=/home/hwellmann/work/org.ops4j.pax.swissbox/pax-swissbox-framework/target/storage",
            "-Dpax.swissbox.framework.rmi.port=1099",
            "-Dpax.swissbox.framework.rmi.name=PaxRemoteFramework"
        };
        javaRunner.exec( vmOptions, buildClasspath(), RemoteFrameworkImpl.class.getName(),
            null, findJavaHome(), null );
    }

    @After
    public void tearDown() throws InterruptedException
    {
        if( javaRunner != null )
        {
            javaRunner.shutdown();
        }
    }

    @Test
    public void forkEquinox() throws BundleException, IOException, InterruptedException,
        NotBoundException
    {
        Registry registry = LocateRegistry.getRegistry();
        Thread.sleep( 1000 );
        RemoteFramework framework = (RemoteFramework) registry.lookup( "PaxRemoteFramework" );
        framework.start();

        long commonsIoId = framework.installBundle( "file:target/bundles/commons-io-2.1.jar" );
        framework.startBundle( commonsIoId );

        framework.stop();
    }

    private String[] buildClasspath()
    {
        String frameworkPath =
            frameworkFactory.getClass().getProtectionDomain().getCodeSource().getLocation()
                .toString();
        String launcherPath =
            RemoteFrameworkImpl.class.getProtectionDomain().getCodeSource().getLocation()
                .toString();
        return new String[]{ frameworkPath, launcherPath };
    }

    private String findJavaHome()
    {
        String javaHome = System.getenv( "JAVA_HOME" );
        if( javaHome == null )
        {
            javaHome = System.getProperty( "java.home" );
        }
        return javaHome;
    }
}
