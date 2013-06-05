package ca.corefacility.bioinformatics.irida.web.controller.test.unit.samples;

import ca.corefacility.bioinformatics.irida.model.Project;
import ca.corefacility.bioinformatics.irida.model.Relationship;
import ca.corefacility.bioinformatics.irida.model.Sample;
import ca.corefacility.bioinformatics.irida.model.SequenceFile;
import ca.corefacility.bioinformatics.irida.model.roles.impl.Identifier;
import ca.corefacility.bioinformatics.irida.service.RelationshipService;
import ca.corefacility.bioinformatics.irida.service.SampleService;
import ca.corefacility.bioinformatics.irida.service.SequenceFileService;
import ca.corefacility.bioinformatics.irida.web.assembler.resource.ResourceCollection;
import ca.corefacility.bioinformatics.irida.web.assembler.resource.sequencefile.SequenceFileResource;
import ca.corefacility.bioinformatics.irida.web.controller.api.GenericController;
import ca.corefacility.bioinformatics.irida.web.controller.api.samples.SampleSequenceFilesController;
import ca.corefacility.bioinformatics.irida.web.controller.links.PageLink;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ui.ModelMap;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link SampleSequenceFilesController}.
 */
public class SampleSequenceFilesControllerTest {
    private SampleSequenceFilesController controller;
    private SequenceFileService sequenceFileService;
    private SampleService sampleService;
    private RelationshipService relationshipService;

    @Before
    public void setUp() {
        sampleService = mock(SampleService.class);
        sequenceFileService = mock(SequenceFileService.class);
        relationshipService = mock(RelationshipService.class);

        controller = new SampleSequenceFilesController(sequenceFileService, sampleService, relationshipService);
    }

    @Test
    public void testGetSampleSequenceFiles() throws IOException {
        Project p = constructProject();
        Sample s = constructSample();
        SequenceFile sf = constructSequenceFile();
        Relationship r = new Relationship();
        r.setIdentifier(new Identifier());
        r.setSubject(s.getIdentifier());
        r.setObject(sf.getIdentifier());
        Collection<Relationship> relationships = Sets.newHashSet(r);

        // mock out the service calls
        when(relationshipService.getRelationshipsForEntity(s.getIdentifier(), Sample.class, SequenceFile.class))
                .thenReturn(relationships);
        when(sequenceFileService.read(sf.getIdentifier())).thenReturn(sf);

        ModelMap modelMap = controller.getSampleSequenceFiles(p.getIdentifier().getIdentifier(), s.getIdentifier().getIdentifier());

        // verify that the service calls were used.
        verify(relationshipService, times(1)).getRelationshipsForEntity(s.getIdentifier(), Sample.class,
                SequenceFile.class);
        verify(sequenceFileService, times(1)).read(sf.getIdentifier());

        Object o = modelMap.get(GenericController.RESOURCE_NAME);
        assertTrue(o instanceof ResourceCollection);
        @SuppressWarnings("unchecked")
        ResourceCollection<SequenceFileResource> resources = (ResourceCollection<SequenceFileResource>) o;
        assertNotNull(resources);
        assertEquals(1, resources.size());
        assertNotNull(resources.getLink(PageLink.REL_SELF));
        assertNotNull(resources.getLink(SampleSequenceFilesController.REL_SAMPLE));
        SequenceFileResource sfr = resources.iterator().next();
        assertNotNull(sfr.getLink(PageLink.REL_SELF));
        assertNotNull(sfr.getLink(GenericController.REL_RELATIONSHIP));
        assertEquals(sf.getFile().getName(), sfr.getFile().getName());
    }

    /**
     * Construct a simple {@link ca.corefacility.bioinformatics.irida.model.Sample}.
     *
     * @return a sample with a name and identifier.
     */
    private Sample constructSample() {
        String sampleId = UUID.randomUUID().toString();
        Identifier sampleIdentifier = new Identifier();
        sampleIdentifier.setIdentifier(sampleId);
        String sampleName = "sampleName";
        Sample s = new Sample();
        s.setSampleName(sampleName);
        s.setIdentifier(sampleIdentifier);
        return s;
    }

    /**
     * Construct a simple {@link SequenceFile}.
     *
     * @return a {@link SequenceFile} with identifier.
     */
    private SequenceFile constructSequenceFile() throws IOException {
        String sequenceFileId = UUID.randomUUID().toString();
        Identifier sequenceFileIdentifier = new Identifier();
        File f = Files.createTempFile(null, null).toFile();
        f.deleteOnExit();
        sequenceFileIdentifier.setIdentifier(sequenceFileId);
        SequenceFile sf = new SequenceFile();
        sf.setIdentifier(sequenceFileIdentifier);
        sf.setFile(f);
        return sf;
    }

    /**
     * Construct a simple {@link Project}.
     *
     * @return a project with a name and identifier.
     */
    private Project constructProject() {
        String projectId = UUID.randomUUID().toString();
        Identifier projectIdentifier = new Identifier();
        projectIdentifier.setIdentifier(projectId);
        Project p = new Project();
        p.setIdentifier(projectIdentifier);
        return p;
    }
}
