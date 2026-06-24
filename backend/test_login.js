const axios = require('axios');

async function test() {
  try {
    const res = await axios.post('http://localhost:8080/api/auth/login', {
      username: 'admin_user',
      password: 'password'
    });
    console.log('Login successful:', res.data);
    const token = res.data.data.token;
    
    const usersRes = await axios.get('http://localhost:8080/api/auth/users', {
      headers: { Authorization: `Bearer ${token}` }
    });
    console.log('Users fetch successful:', usersRes.data);
  } catch (err) {
    console.error('Error:', err.response ? err.response.status + ' ' + err.response.data.message : err.message);
  }
}
test();
