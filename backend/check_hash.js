const bcrypt = require('bcryptjs');
console.log(bcrypt.compareSync('password', '$2a$12$R.S/1aWzP/6G.7jGZtMyqef1v5b3S8/HjQ53K4A6P4L8tVbO9k8v6'));
