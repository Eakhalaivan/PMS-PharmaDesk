const axios = require('axios');
async function test() {
  try {
    const res = await axios.post('http://localhost:8080/api/auth/login', {
      username: 'admin',
      password: 'password'
    });
    console.log('Token:', res.data.data.token);
  } catch (err) {
    console.log('Error:', err.message);
  }
}
test();
