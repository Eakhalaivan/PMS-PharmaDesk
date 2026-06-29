const axios = require('axios');

async function test() {
  try {
    const res = await axios.post('http://localhost:8080/api/auth/login', {
      username: 'admin',
      password: 'pms'
    });
    console.log('Login successful, roles:', res.data.data.roles);
    const token = res.data.data.token;
    
    try {
      const dashRes = await axios.get('http://localhost:8080/api/pharmacy/dashboard/summary', {
        headers: { Authorization: `Bearer ${token}` }
      });
      console.log('Dashboard fetch successful:', Object.keys(dashRes.data));
    } catch(err) {
      console.error('Dashboard Error:', err.response ? err.response.status + ' ' + JSON.stringify(err.response.data) : err.message);
    }
  } catch (err) {
    console.error('Login Error:', err.response ? err.response.status + ' ' + JSON.stringify(err.response.data) : err.message);
  }
}
test();
