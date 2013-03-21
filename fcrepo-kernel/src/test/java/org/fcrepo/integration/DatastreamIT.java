
package org.fcrepo.integration;

import static org.fcrepo.services.DatastreamService.createDatastreamNode;
import static org.fcrepo.services.DatastreamService.getDatastream;
import static org.fcrepo.services.ObjectService.createObjectNode;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.io.IOUtils;
import org.fcrepo.Datastream;
import org.fcrepo.exception.InvalidChecksumException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/spring-test/repo.xml")
public class DatastreamIT extends AbstractIT {

    @Inject
    Repository repo;

    @Test
    public void testLabel() throws Exception {
    	System.out.println("INIT FOR testLABEL");
        Session session = repo.login();
        createObjectNode(session, "testDatastreamObject");
        Node dsNode =
                createDatastreamNode(session,
                        "/objects/testDatastreamObject/testDatastreamNode",
                        "application/octet-stream", new ByteArrayInputStream(
                                "asdf".getBytes()));
        new Datastream(dsNode).setLabel("Best datastream ever!");
        session.save();
        session.logout();
        session = repo.login();
        final Datastream ds = getDatastream("testDatastreamObject", "testDatastreamNode");
        assertEquals("Wrong label!", "Best datastream ever!", ds.getLabel());
    }
    
    @Test
    public void testCreatedDate() throws RepositoryException, IOException, InvalidChecksumException {
        Session session = repo.login();
        createObjectNode(session, "testDatastreamObject");
        createDatastreamNode(session,
                        "/objects/testDatastreamObject/testDatastreamNode1",
                        "application/octet-stream", new ByteArrayInputStream(
                                "asdf".getBytes()));
        session.save();
        session.logout();
        session = repo.login();
        final Datastream ds = getDatastream("testDatastreamObject", "testDatastreamNode1");
        assertNotNull("Couldn't find created date on datastream!", ds.getCreatedDate());
    }

    @Test
    public void testDatastreamContent() throws IOException, RepositoryException, InvalidChecksumException {
        Session session = repo.login();
        createObjectNode(session, "testDatastreamObject");
        createDatastreamNode(session,
                "/objects/testDatastreamObject/testDatastreamNode1",
                "application/octet-stream", new ByteArrayInputStream(
                "asdf".getBytes()));

        session.save();

        final Datastream ds = getDatastream("testDatastreamObject", "testDatastreamNode1");
        String contentString = IOUtils.toString(ds.getContent(), "ASCII");

        assertEquals("asdf", contentString);

    }

    @Test
    public void testDatastreamContentDigestAndLength() throws IOException, RepositoryException, InvalidChecksumException {
        Session session = repo.login();
        createObjectNode(session, "testDatastreamObject");
        createDatastreamNode(session,
                "/objects/testDatastreamObject/testDatastreamNode2",
                "application/octet-stream", new ByteArrayInputStream(
                "asdf".getBytes()));


        session.save();

        final Datastream ds = getDatastream("testDatastreamObject", "testDatastreamNode2");
        assertEquals("urn:sha1:3da541559918a808c2402bba5012f6c60b27661c", ds
                .getContentDigest().toString());
        assertEquals(4L, ds.getContentSize());


        String contentString = IOUtils.toString(ds.getContent(), "ASCII");

        assertEquals("asdf", contentString);
    }

    @Test
    public void testModifyDatastreamContentDigestAndLength() throws IOException, RepositoryException, InvalidChecksumException {
        Session session = repo.login();
        createObjectNode(session, "testDatastreamObject");
        createDatastreamNode(session,
                "/objects/testDatastreamObject/testDatastreamNode3",
                "application/octet-stream", new ByteArrayInputStream(
                "asdf".getBytes()));


        session.save();

        final Datastream ds = getDatastream("testDatastreamObject", "testDatastreamNode3");
        ds.setContent(new ByteArrayInputStream(
                "0123456789".getBytes()));

        assertEquals("urn:sha1:87acec17cd9dcd20a716cc2cf67417b71c8a7016", ds.getContentDigest().toString());
        assertEquals(10L, ds.getContentSize());


        String contentString = IOUtils.toString(ds.getContent(), "ASCII");

        assertEquals("0123456789", contentString);
    }
    
    @Test
    public void testDatastreamContentWithChecksum() throws IOException, RepositoryException, InvalidChecksumException {
        Session session = repo.login();
        createObjectNode(session, "testDatastreamObject");
        createDatastreamNode(session,
                "/objects/testDatastreamObject/testDatastreamNode4",
                "application/octet-stream", new ByteArrayInputStream(
                "asdf".getBytes()), "SHA-1", "3da541559918a808c2402bba5012f6c60b27661c");

        session.save();

        final Datastream ds = getDatastream("testDatastreamObject", "testDatastreamNode4");
        assertEquals("urn:sha1:3da541559918a808c2402bba5012f6c60b27661c", ds
                .getContentDigest().toString());

        String contentString = IOUtils.toString(ds.getContent(), "ASCII");

        assertEquals("asdf", contentString);
    }

}
