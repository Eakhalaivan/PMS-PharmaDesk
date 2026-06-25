const mysql = require('mysql2/promise');
async function test() {
  const conn = await mysql.createConnection({
    host: 'gateway01.ap-southeast-1.prod.alicloud.tidbcloud.com',
    port: 4000,
    user: '3ZMzEHamXVuJdSt.root',
    password: 'o2ldKmVs3hjxipyi',
    database: 'pms',
    ssl: { minVersion: 'TLSv1.2' }
  });
  await conn.execute("DELETE FROM flyway_schema_history WHERE version = '29';");
  console.log("Deleted version 29 from flyway_schema_history");
  conn.end();
}
test();
