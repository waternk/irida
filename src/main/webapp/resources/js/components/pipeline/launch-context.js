import React, { useEffect } from "react";
import { setBaseUrl } from "../../utilities/url-utilities";

const LaunchStateContext = React.createContext();
const LaunchDispatchContext = React.createContext();

function launchReducer(state, action) {
  switch (action.type) {
    case "loaded":
      return { ...state, fetching: true };
    default:
      throw new Error(`Unhandled action type: ${action.type}`);
  }
}

function LaunchProvider({ children, pipelineId, automated = false }) {
  const [state, dispatch] = React.useReducer(launchReducer, {
    fetching: true,
  });

  useEffect(() => {
    fetch(setBaseUrl(`/ajax/pipelines/${pipelineId}?automated=${automated}`))
      .then((response) => response.json())
      .then((json) => {
        console.log(json);
      });
  }, [pipelineId, automated]);

  return (
    <LaunchStateContext.Provider value={state}>
      <LaunchDispatchContext.Provider value={dispatch}>
        {children}
      </LaunchDispatchContext.Provider>
    </LaunchStateContext.Provider>
  );
}

function useLaunchState() {
  const context = React.useContext(LaunchStateContext);
  if (typeof context === "undefined") {
    throw new Error(`useLaunchContext must be used within a LaunchProvider`);
  }
  return context;
}

function useLaunchDispatch() {
  const context = React.useContext(LaunchDispatchContext);
  if (typeof context === "undefined") {
    throw new Error(`useLaunchDispatch must be used within a LaunchDispatch`);
  }
  return context;
}

export { LaunchProvider, useLaunchState, useLaunchDispatch };