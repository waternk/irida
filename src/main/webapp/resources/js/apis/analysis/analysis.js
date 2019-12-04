/**
 * Analysis related API functions
 */
import axios from "axios";

// Ajax URL for Analysis
const ANALYSIS_URL = `${window.TL.BASE_URL}ajax/analysis`;

// Ajax URL for Analyses
const ANALYSES_URL = `${window.TL.BASE_URL}ajax/analyses`;

/*
 * Get all the data required for the analysis -> details page.
 * @param {number} submissionId Submission ID
 * @return {Promise<*>} `data` contains the OK response and the details map;
 *                      `error` contains error information if an error occurred.
 */
export async function getVariablesForDetails(submissionId) {
  const { data } = await axios.get(`${ANALYSIS_URL}/details/${submissionId}`);
  return data;
}

/*
 * Get analysis input files
 * @param {number} submissionId Submission ID
 * @return {Promise<*>} `data` contains the OK response and input files data;
 *                      `error` contains error information if an error occurred.
 */
export async function getAnalysisInputFiles(submissionId) {
  try {
    const { data } = await axios.get(`${ANALYSIS_URL}/inputs/${submissionId}`);
    return data;
  } catch (error) {
    return { samples: [], referenceFile: null };
  }
}

/*
 * Updates user preference to either receive or not receive an email on
 * analysis error or completion.
 * @param {number} submissionId Submission ID
 * @param {boolean} emailPipelineResult True or False
 * @return {Promise<*>} `data` contains the OK response; error` contains error information if an error occurred.
 */
export async function updateAnalysisEmailPipelineResult({
  submissionId,
  emailPipelineResult
}) {
  try {
    const { data } = await axios.patch(
      `${ANALYSIS_URL}/update-email-pipeline-result`,
      {
        analysisSubmissionId: submissionId,
        emailPipelineResult: emailPipelineResult
      }
    );
    return data.message;
  } catch (error) {
    return { text: error.response.data.message, type: "error" };
  }
}

/*
 * Updates analysis name and/or analysis priority.
 * @param {number} submissionId Submission ID
 * @param {string} analysisName Name of analysis
 * @param {string} priority [LOW, MEDIUM, HIGH]
 * @return {Promise<*>} `data` contains the OK response; `error` contains error information if an error occurred.
 */
export async function updateAnalysis({ submissionId, analysisName, priority }) {
  try {
    const { data } = await axios.patch(`${ANALYSIS_URL}/update-analysis/`, {
      analysisSubmissionId: submissionId,
      analysisName: analysisName,
      priority: priority
    });
    return data.message;
  } catch (error) {
    return { text: error.response.data.message, type: "error" };
  }
}

/*
 * Deletes the analysis.
 * @param {number} submissionID Submission ID
 * @return {Promise<*>} `data` contains the OK response; `error` contains error information if an error occurred.
 */
export async function deleteAnalysis(submissionId) {
  const { data } = await axios.delete(`${ANALYSIS_URL}/delete/${submissionId}`);
  return data;
}

/*
 * Gets all projects that this analysis can be shared with.
 * @param {number} submissionID Submission ID
 * @return {Promise<*>} `data` contains the OK response; `error` contains error information if an error occurred.
 */
export async function getSharedProjects(submissionId) {
  const { data } = await axios.get(`${ANALYSIS_URL}/${submissionId}/share`);
  return data;
}

/*
 * Updates whether or not an analysis is shared with a project.
 * @param {number} submissionID Submission ID
 * @param {number} projectID Project ID
 * @param {boolean} shareStatus True of False
 * @return {Promise<*>} `data` contains the OK response; `error` contains error information if an error occurred.
 */
export async function updateSharedProject({
  submissionId,
  projectId,
  shareStatus
}) {
  const { data } = await axios.post(`${ANALYSIS_URL}/${submissionId}/share`, {
    projectId: projectId,
    shareStatus: shareStatus
  });
  return data;
}

/*
 * Saves analysis to related samples.
 * @param {number} submissionID Submission ID
 * @return {Promise<*>} `data` contains the OK response; `error` contains error information if an error occurred.
 */
export async function saveToRelatedSamples(submissionId) {
  try {
    const { data } = await axios.post(
      `${ANALYSIS_URL}/${submissionId}/save-results`
    );
    return data.message;
  } catch (error) {
    return { text: error.response.data.message, type: "error" };
  }
}

/**
 * Get the job errors.
 * @param {number} submissionID Submission ID
 * @return {Promise<*>} `data` contains the OK response.
 */
export async function getJobErrors(submissionId) {
  try {
    const { data } = await axios.get(
      `${ANALYSIS_URL}/${submissionId}/job-errors`
    );
    return data;
  } catch (error) {
    return { error };
  }
}

/**
 * Get the sistr results.
 * @param {number} submissionID Submission ID
 * @return {Promise<*>} `data` contains the OK response; `error` contains error information if an error occurred.
 */
export async function getSistrResults(submissionId) {
  try {
    const { data } = await axios.get(`${ANALYSIS_URL}/sistr/${submissionId}`);
    return data;
  } catch (error) {
    return { error };
  }
}

/**
 * Get the output file info
 * @param {number} submissionID Submission ID
 * @return {Promise<*>} `data` contains the OK response; `error` contains error information if an error occurred.
 */
export async function getOutputInfo(submissionId) {
  try {
    const res = await axios.get(`${ANALYSIS_URL}/${submissionId}/outputs`);
    return res.data;
  } catch (error) {
    return { error };
  }
}

/**
 * Get the data from the output file for with the supplied chunk size
 * @param {object} contains the output file data
 * @return {Promise<*>} `data` contains the OK response; `error` contains error information if an error occurred.
 */
export async function getDataViaChunks({ submissionId, fileId, seek, chunk }) {
  try {
    const res = await axios.get(
      `${ANALYSIS_URL}/${submissionId}/outputs/${fileId}`,
      {
        params: {
          seek,
          chunk
        }
      }
    );
    return res.data;
  } catch (error) {
    return { error };
  }
}

/**
 * Get the file output from line x upto line y.
 * @param {object} contains the output file data
 * @return {Promise<*>} `data` contains the OK response; `error` contains error information if an error occurred.
 */
export async function getDataViaLines({ submissionId, fileId, start, end }) {
  try {
    const res = await axios.get(
      `${ANALYSIS_URL}/${submissionId}/outputs/${fileId}`,
      {
        params: {
          start,
          end
        }
      }
    );
    return res.data;
  } catch (error) {
    return { error };
  }
}

/**
 * Download output file using an analysis submission id and file id.
 * @param {submissionId} submission for which to download output file for.
 * @param {fileId} file id of file to download.
 * @return {Promise<*>} `data` contains the OK response; `error` contains error information if an error occurred.
 */
export function downloadOutputFile({ submissionId, fileId }) {
  window.open(
    `${ANALYSIS_URL}/download/${submissionId}/file/${fileId}`,
    "_blank"
  );
}

/**
 * Get the newick string for the submission.
 * @return {Promise<*>} `data` contains the OK response; `error` contains error information if an error occurred.
 */
export async function getNewickTree(submissionId) {
  try {
    const res = await axios.get(`${URL}/${submissionId}/tree`);
    return res.data;
  } catch (error) {
    return { error: error };
  }
}

/**
 * Get all single sample analysis output file info for the principal user.
 * @return {Promise<*>} `data` contains the OK response; `error` contains error information if an error occurred.
 */
export async function getPrincipalUserSingleSampleAnalysisOutputs() {
  try {
    const { data } = await axios.get(`${ANALYSES_URL}user/analysis-outputs`);
    return { data };
  } catch (error) {
    return { error };
  }
}

/**
 * Get all shared single sample analysis output file info for a project.
 * @param projectId Project ID
 * @return {Promise<*>} `data` contains the OK response; `error` contains error information if an error occurred.
 */
export async function getProjectSharedSingleSampleAnalysisOutputs(projectId) {
  try {
    const { data } = await axios.get(
      `${ANALYSIS_URL}/project/${projectId}/shared-analysis-outputs`
    );
    return { data };
  } catch (error) {
    return { error };
  }
}

/**
 * Get all automated single sample analysis output file info for a project.
 * @param {number} projectId Project ID
 * @return {Promise<*>} `data` contains the OK response; `error` contains error information if an error occurred.
 */
export async function getProjectAutomatedSingleSampleAnalysisOutputs(
  projectId
) {
  try {
    const { data } = await axios.get(
      `${ANALYSIS_URL}/project/${projectId}/automated-analysis-outputs`
    );
    return { data };
  } catch (error) {
    return { error };
  }
}

/**
 * Prepare download of multiple analysis output files using a list of analysis output file info object.
 * @param {Array<Object>} outputs List of analysis output file info to prepare download of.
 * @return {Promise<*>} `data` contains the OK response; `error` contains error information if an error occurred.
 */
export async function prepareAnalysisOutputsDownload(outputs) {
  try {
    const { data } = await axios({
      method: "post",
      url: `${ANALYSIS_URL}/download/prepare`,
      data: outputs
    });
    return { data };
  } catch (error) {
    return { error };
  }
}

export async function fetchAllPipelinesStates() {
  return axios.get(`${ANALYSES_URL}/states`).then(response => response.data);
}

export async function fetchAllPipelinesTypes() {
  return axios.get(`${ANALYSES_URL}/types`).then(response => response.data);
}

export async function deleteAnalysisSubmissions({ ids }) {
  return axios.delete(`${ANALYSES_URL}/delete?ids=${ids.join(",")}`);
}
