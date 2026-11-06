import apiServer from '../server/index.js';

export default function handler(req, res) {
  return apiServer.emit('request', req, res);
}
