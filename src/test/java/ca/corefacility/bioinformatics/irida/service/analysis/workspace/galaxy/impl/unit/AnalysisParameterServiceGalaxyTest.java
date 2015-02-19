package ca.corefacility.bioinformatics.irida.service.analysis.workspace.galaxy.impl.unit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ca.corefacility.bioinformatics.irida.exceptions.IridaWorkflowParameterException;
import ca.corefacility.bioinformatics.irida.model.workflow.IridaWorkflow;
import ca.corefacility.bioinformatics.irida.model.workflow.description.IridaToolParameter;
import ca.corefacility.bioinformatics.irida.model.workflow.description.IridaWorkflowDescription;
import ca.corefacility.bioinformatics.irida.model.workflow.description.IridaWorkflowParameter;
import ca.corefacility.bioinformatics.irida.model.workflow.execution.galaxy.WorkflowInputsGalaxy;
import ca.corefacility.bioinformatics.irida.service.analysis.workspace.galaxy.AnalysisParameterServiceGalaxy;
import ca.corefacility.bioinformatics.irida.service.analysis.workspace.galaxy.ParameterBuilderGalaxy;

import com.github.jmchilton.blend4j.galaxy.beans.WorkflowInputs;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Tests for the {@link AnalysisParameterServiceGalaxy} class.
 * 
 * @author Aaron Petkau <aaron.petkau@phac-aspc.gc.ca>
 *
 */
public class AnalysisParameterServiceGalaxyTest {

	@Mock
	private IridaWorkflow iridaWorkflow;

	@Mock
	private IridaWorkflowDescription iridaWorkflowDescription;

	private AnalysisParameterServiceGalaxy analysisParameterService;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);

		analysisParameterService = new AnalysisParameterServiceGalaxy();

		when(iridaWorkflow.getWorkflowDescription()).thenReturn(iridaWorkflowDescription);

		IridaToolParameter iridaToolParameter = new IridaToolParameter("galaxy-tool1", "parameter1");
		IridaWorkflowParameter parameter1 = new IridaWorkflowParameter("parameter1", "0",
				Lists.newArrayList(iridaToolParameter));
		List<IridaWorkflowParameter> iridaWorkflowParameters = Lists.newArrayList(parameter1);

		when(iridaWorkflowDescription.getParameters()).thenReturn(iridaWorkflowParameters);
		when(iridaWorkflowDescription.acceptsParameters()).thenReturn(true);
	}

	/**
	 * Tests preparing workflow parameters and overriding with custom value
	 * successfully.
	 * 
	 * @throws IridaWorkflowParameterException
	 */
	@Test
	public void testPrepareParametersOverrideSuccess() throws IridaWorkflowParameterException {
		Map<String, String> parameters = Maps.newHashMap();
		parameters.put("parameter1", "1");

		WorkflowInputsGalaxy workflowInputsGalaxy = analysisParameterService.prepareAnalysisParameters(parameters,
				iridaWorkflow);

		assertNotNull("workflowInputsGalaxy is null", workflowInputsGalaxy);

		WorkflowInputs workflowInputs = workflowInputsGalaxy.getInputsObject();
		Map<Object, Map<String, Object>> workflowParameters = workflowInputs.getParameters();
		Map<String, Object> tool1Parameters = workflowParameters.get("galaxy-tool1");
		assertNotNull("parameters for galaxy-tool1 should not be null", tool1Parameters);

		assertEquals("galaxy-tool1,parameter1 is not valid", "1", tool1Parameters.get("parameter1"));
	}
	
	/**
	 * Tests preparing workflow parameters with multiple levels and overriding
	 * with custom value successfully.
	 * 
	 * @throws IridaWorkflowParameterException
	 */
	@Test
	public void testPrepareParametersOverrideMultipleLevelSuccess() throws IridaWorkflowParameterException {
		IridaToolParameter iridaToolParameter = new IridaToolParameter("galaxy-tool1", "level1.parameter1");
		IridaWorkflowParameter parameter1 = new IridaWorkflowParameter("parameter1", "0",
				Lists.newArrayList(iridaToolParameter));
		List<IridaWorkflowParameter> iridaWorkflowParameters = Lists.newArrayList(parameter1);

		when(iridaWorkflowDescription.getParameters()).thenReturn(iridaWorkflowParameters);

		Map<String, String> parameters = Maps.newHashMap();
		parameters.put("parameter1", "1");

		WorkflowInputsGalaxy workflowInputsGalaxy = analysisParameterService.prepareAnalysisParameters(parameters,
				iridaWorkflow);

		assertNotNull("workflowInputsGalaxy is null", workflowInputsGalaxy);

		WorkflowInputs workflowInputs = workflowInputsGalaxy.getInputsObject();
		Map<Object, Map<String, Object>> workflowParameters = workflowInputs.getParameters();
		Map<String, Object> tool1Parameters = workflowParameters.get("galaxy-tool1");
		assertNotNull("parameters for galaxy-tool1 should not be null", tool1Parameters);

		assertEquals("parameter not properly defined", ImmutableMap.of("level1", ImmutableMap.of("parameter1", "1")),
				tool1Parameters);
	}

	/**
	 * Tests preparing workflow parameters and using the default value defined.
	 * 
	 * @throws IridaWorkflowParameterException
	 */
	@Test
	public void testPrepareParametersDefaultSuccess() throws IridaWorkflowParameterException {
		Map<String, String> parameters = Maps.newHashMap();

		WorkflowInputsGalaxy workflowInputsGalaxy = analysisParameterService.prepareAnalysisParameters(parameters,
				iridaWorkflow);
		assertNotNull("workflowInputsGalaxy is null", workflowInputsGalaxy);

		WorkflowInputs workflowInputs = workflowInputsGalaxy.getInputsObject();
		Map<Object, Map<String, Object>> workflowParameters = workflowInputs.getParameters();
		Map<String, Object> tool1Parameters = workflowParameters.get("galaxy-tool1");
		assertNotNull("parameters for galaxy-tool1 should not be null", tool1Parameters);

		assertEquals("galaxy-tool1,parameter1 is not valid", "0", tool1Parameters.get("parameter1"));
	}
	
	/**
	 * Tests preparing workflow parameters and ignoring the default value successfully.
	 * 
	 * @throws IridaWorkflowParameterException
	 */
	@Test
	public void testPrepareParametersIgnoreDefaultSuccess() throws IridaWorkflowParameterException {
		Map<String, String> parameters = Maps.newHashMap();
		parameters.put("parameter1", IridaWorkflowParameter.IGNORE_DEFAULT_VALUE);

		WorkflowInputsGalaxy workflowInputsGalaxy = analysisParameterService.prepareAnalysisParameters(parameters,
				iridaWorkflow);
		assertNotNull("workflowInputsGalaxy is null", workflowInputsGalaxy);

		WorkflowInputs workflowInputs = workflowInputsGalaxy.getInputsObject();
		Map<Object, Map<String, Object>> workflowParameters = workflowInputs.getParameters();
		assertNull("should be no parameter set for galaxy-tool1", workflowParameters.get("galaxy-tool1"));
	}

	/**
	 * Tests preparing workflow parameters and overriding with custom value
	 * successfully in two tools.
	 * 
	 * @throws IridaWorkflowParameterException
	 */
	@Test
	public void testPrepareParametersOverrideSuccessTwoTools() throws IridaWorkflowParameterException {
		Map<String, String> parameters = Maps.newHashMap();
		parameters.put("parameter1", "1");

		IridaToolParameter iridaToolParameter = new IridaToolParameter("galaxy-tool1", "parameter1");
		IridaToolParameter iridaToolParameter2 = new IridaToolParameter("galaxy-tool1", "parameter2");
		IridaWorkflowParameter parameter1 = new IridaWorkflowParameter("parameter1", "0", Lists.newArrayList(
				iridaToolParameter, iridaToolParameter2));
		List<IridaWorkflowParameter> iridaWorkflowParameters = Lists.newArrayList(parameter1);

		when(iridaWorkflowDescription.getParameters()).thenReturn(iridaWorkflowParameters);

		WorkflowInputsGalaxy workflowInputsGalaxy = analysisParameterService.prepareAnalysisParameters(parameters,
				iridaWorkflow);

		assertNotNull("workflowInputsGalaxy is null", workflowInputsGalaxy);

		WorkflowInputs workflowInputs = workflowInputsGalaxy.getInputsObject();
		Map<Object, Map<String, Object>> workflowParameters = workflowInputs.getParameters();

		Map<String, Object> tool1Parameters = workflowParameters.get("galaxy-tool1");
		assertNotNull("parameters for galaxy-tool1 should not be null", tool1Parameters);
		assertEquals("galaxy-tool1,parameter1 is not valid", "1", tool1Parameters.get("parameter1"));
		assertEquals("galaxy-tool1,parameter2 is not valid", "1", tool1Parameters.get("parameter2"));
	}

	/**
	 * Tests preparing workflow parameters and failing due to parameters not
	 * defined within the IRIDA workflow.
	 * 
	 * @throws IridaWorkflowParameterException
	 */
	@Test(expected = IridaWorkflowParameterException.class)
	public void testPrepareParametersOverrideFail() throws IridaWorkflowParameterException {
		Map<String, String> parameters = Maps.newHashMap();
		parameters.put("parameter-invalid", "1");

		analysisParameterService.prepareAnalysisParameters(parameters, iridaWorkflow);
	}
	
	/**
	 * Tests preparing workflow parameters when there are no parameters to prepare.
	 * 
	 * @throws IridaWorkflowParameterException
	 */
	@Test
	public void testPrepareParametersSuccessNoParameters() throws IridaWorkflowParameterException {
		when(iridaWorkflowDescription.acceptsParameters()).thenReturn(false);
		
		WorkflowInputsGalaxy workflowInputsGalaxy = analysisParameterService.prepareAnalysisParameters(ImmutableMap.of(),
				iridaWorkflow);

		assertNotNull("workflowInputsGalaxy is null", workflowInputsGalaxy);

		WorkflowInputs workflowInputs = workflowInputsGalaxy.getInputsObject();
		assertNotNull("workflowInputs is null", workflowInputs);
		
		verify(iridaWorkflowDescription).acceptsParameters();
	}
	
	/**
	 * Tests failing to prepare workflow parameters when there are some parameters passed but the workflow accepts no parameters.
	 * 
	 * @throws IridaWorkflowParameterException
	 */
	@Test(expected=IridaWorkflowParameterException.class)
	public void testPrepareParametersSuccessNoAcceptParametersWithParameters() throws IridaWorkflowParameterException {
		when(iridaWorkflowDescription.acceptsParameters()).thenReturn(false);
		
		analysisParameterService.prepareAnalysisParameters(ImmutableMap.of("name", "value"),
				iridaWorkflow);
	}
	
	/**
	 * Tests out failing to build a parameter builder due to null parameters.
	 */
	@Test(expected = NullPointerException.class)
	public void testParameterBuilderGalaxyFailNullParameter() {
		new ParameterBuilderGalaxy(null);
	}

	/**
	 * Tests out failing to build a parameter builder due to no parameters
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testParameterBuilderGalaxyFailNoParameter() {
		new ParameterBuilderGalaxy("");
	}

	/**
	 * Tests out building a data structure with only one parameter mapping for
	 * Galaxy.
	 */
	@Test
	public void testParameterBuilderGalaxySuccessOneParameter() {
		ParameterBuilderGalaxy parameterBuilder = new ParameterBuilderGalaxy("parameter");
		assertEquals("parameter", parameterBuilder.getStartName());
		assertEquals("value", parameterBuilder.buildForValue("value"));
	}

	/**
	 * Tests out building a data structure with only a second level for
	 * parameter mapping for Galaxy.
	 */
	@Test
	public void testParameterBuilderGalaxySuccessTwoParameters() {
		ParameterBuilderGalaxy parameterBuilder = new ParameterBuilderGalaxy("level1.parameter");
		assertEquals("level1", parameterBuilder.getStartName());
		assertEquals(ImmutableMap.of("parameter", "value"), parameterBuilder.buildForValue("value"));
	}

	/**
	 * Tests out building a data structure with a third level for a parameter.
	 */
	@Test
	public void testParameterBuilderGalaxySuccessThreeParameters() {
		ParameterBuilderGalaxy parameterBuilder = new ParameterBuilderGalaxy("level1.level2.parameter");
		assertEquals("level1", parameterBuilder.getStartName());
		assertEquals(ImmutableMap.of("level2", ImmutableMap.of("parameter", "value")),
				parameterBuilder.buildForValue("value"));
	}
}
