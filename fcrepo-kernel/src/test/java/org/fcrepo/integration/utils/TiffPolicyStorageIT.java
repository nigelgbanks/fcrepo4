
package org.fcrepo.integration.utils;

import org.fcrepo.binary.MimeTypePolicy;
import org.fcrepo.binary.PolicyDecisionPoint;
import org.fcrepo.services.DatastreamService;
import org.fcrepo.services.LowLevelStorageService;
import org.fcrepo.services.ObjectService;
import org.fcrepo.services.functions.GetBinaryKey;
import org.fcrepo.utils.LowLevelCacheEntry;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.modeshape.jcr.JcrRepositoryFactory;
import org.modeshape.jcr.value.BinaryKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

public class TiffPolicyStorageIT {

    protected Logger logger;

    static private Repository repo;

    private DatastreamService datastreamService;

    private ObjectService objectService;

    private LowLevelStorageService lowLevelService;

	private PolicyDecisionPoint pdp;

	GetBinaryKey getBinaryKey = new GetBinaryKey();

    @Before
    public void setLogger() {
        logger = LoggerFactory.getLogger(this.getClass());
    }

    @Before
    public void setRepository() throws RepositoryException {

        final URL config =
                this.getClass().getClassLoader().getResource(
                        "test_composite_storage_repository.json");
        repo =
                new JcrRepositoryFactory().getRepository(config.toString(),
                        null);

		pdp = new PolicyDecisionPoint();
		pdp.addPolicy(new MimeTypePolicy("image/tiff", "tiff-store"));

        datastreamService = new DatastreamService();
        datastreamService.setRepository(repo);
		datastreamService.setStoragePolicyDecisionPoint(pdp);
        objectService = new ObjectService();
        objectService.setRepository(repo);
        lowLevelService = new LowLevelStorageService();
        lowLevelService.setRepository(repo);
    }

    @Test
	@Ignore
    public void testPolicyDrivenStorage() throws Exception {
        final Session session = repo.login();

        objectService.createObject(session, "/testCompositeObject");

        datastreamService.createDatastreamNode(session,
                "/testCompositeObject/content",
                "application/octet-stream", new ByteArrayInputStream(
                        "9876543219876543210987654321098765432109876543210987654321098765432109876543210987654321009876543210".getBytes()));
        datastreamService.createDatastreamNode(session,
                "/testCompositeObject/tiffContent",
                "image/tiff", new ByteArrayInputStream(
                        "87acec17cd9dcd20a716cc2cf67417b71c8a701687acec17cd9dcd20a716cc2cf67417b71c8a701687acec17cd9dcd20a716cc2cf67417b71c8a701687acec17cd9dcd20a716cc2cf67417b71c8a701687acec17cd9dcd20a716cc2cf67417b71c8a701687acec17cd9dcd20a716cc2cf67417b71c8a701687acec17cd9dcd20a716cc2cf67417b71c8a7016".getBytes()));

        session.save();

		final Node node = session.getNode("/testCompositeObject/content");

		BinaryKey key = getBinaryKey.apply(node);

		logger.info("content key: {}", key);


		final Node tiffNode = session.getNode("/testCompositeObject/tiffContent");

		BinaryKey tiffKey = getBinaryKey.apply(tiffNode);

		logger.info("tiff key: {}", tiffKey);

		final Set<LowLevelCacheEntry> lowLevelContentEntries = lowLevelService.getLowLevelCacheEntries(key);

		final Iterator<LowLevelCacheEntry> iterator = lowLevelContentEntries.iterator();

		assertEquals(1, lowLevelContentEntries.size());

		final Set<LowLevelCacheEntry> lowLevelTiffEntries = lowLevelService.getLowLevelCacheEntries(tiffKey);

		final Iterator<LowLevelCacheEntry> tiffIterator = lowLevelTiffEntries.iterator();

		assertEquals(1, lowLevelTiffEntries.size());

		LowLevelCacheEntry e = iterator.next();

		assertThat(e.getExternalIdentifier(), containsString("TransientBinaryStore"));

		LowLevelCacheEntry tiffEntry = tiffIterator.next();
		assertThat(tiffEntry.getExternalIdentifier(), containsString("FileSystemBinaryStore"));


	}
}
