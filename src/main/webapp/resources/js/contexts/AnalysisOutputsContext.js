/*
 * This file contains the state and functions
 * required for displaying analysis outputs.
 */

import React, { useContext, useEffect, useState } from "react";
import { AnalysisContext } from "../contexts/AnalysisContext";

// Functions required by context
import { getOutputInfo } from "../apis/analysis/analysis";

const initialContext = {
  outputs: null,
  fileTypes: [{ hasJsonFile: false, hasTabularFile: false, hasTextFile: false }]
};

const AnalysisOutputsContext = React.createContext(initialContext);
const blacklistExtSet = new Set(["zip", "pdf", "html", "xls"]);
const jsonExtSet = new Set(["json"]);
const tabExtSet = new Set(["tab", "tsv", "tabular", "csv"]);
const excelFileExtSet = new Set(["xlsx"]);

function AnalysisOutputsProvider(props) {
  const [analysisOutputsContext, setAnalysisOutputsContext] = useState(
    initialContext
  );
  const { analysisContext } = useContext(AnalysisContext);

  function getAnalysisOutputs() {
    let hasJsonFile = false;
    let hasTabularFile = false;
    let hasTextFile = false;
    let hasExcelFile = false;

    getOutputInfo(analysisContext.analysis.identifier).then(data => {
      // Check if json, tab, and/or text files exist
      // Used by output file preview to only display
      // tabs that are required

      if (data !== "") {
        data.find(function(el) {
          if (!hasJsonFile) {
            hasJsonFile = jsonExtSet.has(el.fileExt);
          }

          if (!hasTabularFile) {
            hasTabularFile = tabExtSet.has(el.fileExt);
          }

          if (!hasTextFile) {
            hasTextFile =
              !tabExtSet.has(el.fileExt) &&
              !jsonExtSet.has(el.fileExt) &&
              !blacklistExtSet.has(el.fileExt) &&
              !excelFileExtSet.has(el.fileExt);
          }

          if(!hasExcelFile) {
            hasExcelFile = excelFileExtSet.has(el.fileExt);
          }

        });
      }

      setAnalysisOutputsContext(analysisOutputsContext => {
        return {
          ...analysisOutputsContext,
          outputs: data,
          fileTypes: [
            {
              hasJsonFile: hasJsonFile,
              hasTabularFile: hasTabularFile,
              hasTextFile: hasTextFile,
              hasExcelFile: hasExcelFile
            }
          ]
        };
      });
    });
  }

  return (
    <AnalysisOutputsContext.Provider
      value={{
        analysisOutputsContext,
        getAnalysisOutputs,
        tabExtSet,
        jsonExtSet,
        blacklistExtSet,
        excelFileExtSet
      }}
    >
      {props.children}
    </AnalysisOutputsContext.Provider>
  );
}
export { AnalysisOutputsContext, AnalysisOutputsProvider };