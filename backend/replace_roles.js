const fs = require('fs');
const path = require('path');

function walk(dir, callback) {
  const files = fs.readdirSync(dir);
  for (const file of files) {
    const fullPath = path.join(dir, file);
    if (fs.statSync(fullPath).isDirectory()) {
      walk(fullPath, callback);
    } else {
      if (fullPath.endsWith('.java')) {
        callback(fullPath);
      }
    }
  }
}

const dir = '/Users/eakhalaivan/Downloads/PMS-PharmaDesk/backend/src/main/java';
let count = 0;
walk(dir, (file) => {
  let content = fs.readFileSync(file, 'utf8');
  let changed = false;

  // Replace hasAnyRole('A','B') with hasAnyAuthority('ROLE_A','ROLE_B')
  if (content.includes('hasAnyRole')) {
    content = content.replace(/hasAnyRole\(([^)]+)\)/g, (match, args) => {
      const parts = args.split(',').map(s => s.trim());
      const newArgs = parts.map(s => {
        // s might be 'SYSTEM_ADMIN'
        if (s.startsWith("'") && s.endsWith("'")) {
          const inner = s.substring(1, s.length - 1);
          return "'ROLE_" + inner + "'";
        }
        if (s.startsWith('"') && s.endsWith('"')) {
          const inner = s.substring(1, s.length - 1);
          return '"ROLE_' + inner + '"';
        }
        return s; // keep as is
      });
      return `hasAnyAuthority(${newArgs.join(',')})`;
    });
    changed = true;
  }

  // Replace hasRole('A') with hasAuthority('ROLE_A')
  if (content.includes('hasRole')) {
    content = content.replace(/hasRole\(([^)]+)\)/g, (match, arg) => {
      let s = arg.trim();
      if (s.startsWith("'") && s.endsWith("'")) {
        const inner = s.substring(1, s.length - 1);
        return "hasAuthority('ROLE_" + inner + "')";
      }
      if (s.startsWith('"') && s.endsWith('"')) {
        const inner = s.substring(1, s.length - 1);
        return 'hasAuthority("ROLE_' + inner + '")';
      }
      return match;
    });
    changed = true;
  }

  if (changed) {
    fs.writeFileSync(file, content, 'utf8');
    console.log('Updated:', file);
    count++;
  }
});
console.log(`Updated ${count} files.`);
