package org.drools.cdi;

import static org.junit.Assert.fail;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.drools.command.impl.CommandFactoryServiceImpl;
import org.drools.compiler.io.memory.MemoryFileSystem;
import org.drools.core.util.FileManager;
import org.drools.io.impl.ResourceFactoryServiceImpl;
import org.drools.kproject.AbstractKnowledgeTest;
import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.environment.se.discovery.AbstractWeldSEDeployment;
import org.jboss.weld.environment.se.discovery.ImmutableBeanDeploymentArchive;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.junit.AfterClass;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.kie.KieServices;
import org.kie.builder.impl.KieRepositoryImpl;
import org.kie.builder.impl.KieServicesImpl;

public class CDITestRunner extends BlockJUnit4ClassRunner {

    public static volatile Weld          weld;
    public static volatile WeldContainer container;

    public static volatile FileManager   fileManager;

    public static volatile ClassLoader   origCl;

    public static void setUp() {
        fileManager = new FileManager();
        fileManager.setUp();
        origCl = Thread.currentThread().getContextClassLoader();

        MemoryFileSystem mfs = new MemoryFileSystem();
        mfs.write( "META-INF/beans.xml",
                   AbstractKnowledgeTest.generateBeansXML().getBytes() );
        mfs.writeAsJar( CDITestRunner.fileManager.getRootDirectory(),
                        "jar1" );
        java.io.File file1 = CDITestRunner.fileManager.newFile( "jar1.jar" );

        URLClassLoader urlClassLoader;
        try {
            urlClassLoader = new URLClassLoader( new URL[]{file1.toURI().toURL()},
                                                 Thread.currentThread().getContextClassLoader() );
            Thread.currentThread().setContextClassLoader( urlClassLoader );
        } catch ( MalformedURLException e ) {
            fail( e.getMessage() );
        }
    }

    public static void tearDown() {
        try {
            if ( CDITestRunner.weld != null ) { 
                CDITestRunner.weld.shutdown();
             
                CDITestRunner.weld = null;
            }
            if ( CDITestRunner.container != null ) {
                CDITestRunner.container = null; 
            }
        } finally {
            try {
                Thread.currentThread().setContextClassLoader( origCl );                
            } finally {
                fileManager.tearDown();
            }            
        }        
    }
    
    public CDITestRunner(Class cls) throws InitializationError {
        super( cls );
    }

    @Override
    protected Object createTest() throws Exception {
        return container.instance().select( getTestClass().getJavaClass() ).get();
    }

    public static Weld createWeld(String... classes) {
        final List<String> list = new ArrayList<String>();
        list.addAll( Arrays.asList( classes ) );
        //        list.add( KieCDIExtension.class.getName() );
        //        list.add( KBase.class.getName() );
        //        list.add( KSession.class.getName() );
        //        list.add( KReleaseId.class.getName() );
        //        list.add( KieServices.class.getName() );
               list.add( KieServicesImpl.class.getName() );
        //        list.add( KieRepository.class.getName() );
                list.add( KieRepositoryImpl.class.getName() );
        //        list.add( KieCommands.class.getName() );
                list.add( CommandFactoryServiceImpl.class.getName() );
        //        list.add( KieResources.class.getName() );
                list.add( ResourceFactoryServiceImpl.class.getName() );        

        Weld weld = new Weld() {
            @Override
            protected Deployment createDeployment(ResourceLoader resourceLoader,
                                                  Bootstrap bootstrap) {
                return new TestWeldSEDeployment( resourceLoader,
                                                 bootstrap,
                                                 list );
            }
        };
        return weld;
    }

    public static class TestWeldSEDeployment extends AbstractWeldSEDeployment {
        private final BeanDeploymentArchive beanDeploymentArchive;

        public TestWeldSEDeployment(final ResourceLoader resourceLoader,
                                    Bootstrap bootstrap,
                                    List<String> classes) {
            super( bootstrap );
            //            ResourceLoader interceptor = new ResourceLoader() {
            //                
            //                @Override
            //                public void cleanup() {
            //                    resourceLoader.cleanup();
            //                    //WeldSEUrlDeployment.BEANS_XML
            //                }
            //                
            //                @Override
            //                public Collection<URL> getResources(String name) {
            //                    resourceLoader.getResources( name );
            //                    return null;
            //                }
            //                
            //                @Override
            //                public URL getResource(String name) {
            //                    if ( name.equals( WeldSEUrlDeployment.BEANS_XML ) ) {
            //                        try {
            //                            return new URL("http://www.redhat.com");
            //                        } catch ( MalformedURLException e ) {
            //                            //fail("");
            //                        }
            //                    }
            //                    return resourceLoader.getResource( name );
            //                }
            //                
            //                @Override
            //                public Class< ? > classForName(String name) {
            //                    return resourceLoader.classForName( name );
            //                }
            //            };
            beanDeploymentArchive = new ImmutableBeanDeploymentArchive( "classpath",
                                                                        classes,
                                                                        null );

        }

        public Collection<BeanDeploymentArchive> getBeanDeploymentArchives() {
            return Collections.singletonList( beanDeploymentArchive );
        }

        public BeanDeploymentArchive loadBeanDeploymentArchive(Class< ? > beanClass) {
            return beanDeploymentArchive;
        }
    }
}