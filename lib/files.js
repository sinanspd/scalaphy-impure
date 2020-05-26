const fs = require('fs');
const path = require('path');
const ejs = require('ejs');
const chalk = require('chalk');

module.exports = {
  getCurrentDirectoryBase: () => {
    return path.basename(process.cwd());
  },

  directoryExists: (filePath) => {
    return fs.existsSync(filePath);
  },

  renderAndWriteBatch: (templatePaths, paths, data) => {
    zipArrays(paths, data, templatePaths).forEach(pair => {
      ejs.renderFile(pair[0], pair[1], function(err, str){
        if(err){console.log(chalk.red(err))}
        else{
          fs.writeFile(pair[2], str, function(err){
            if(err) console.log(chalk.red(err))
            else console.log(chalk.green(`Created ${pair[0]}`));
          })
        }
      })
    });
  }
};

function zipArrays(a1, a2, a3){
  const concat = xs =>
      xs.length > 0 ? (() => {
          const unit = [];
          return unit.concat.apply(unit, xs);
      })() : [];

  const transpose = xs => xs[0].map((_, col) => xs.map(row => row[col]));

  return transpose([a1, a2, a3]).map(concat)
}