/** Export the BASE URL for the java API */

const hostname = window && window.location && window.location.hostname;
export const ROOT_URL = process.env.NODE_ENV === 'development' ? `http://${hostname}:2017/api`: `http://${hostname}/api`;




