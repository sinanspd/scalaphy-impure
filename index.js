const chalk = require('chalk');
const clear = require('clear');
const figlet = require('figlet');

const files = require('./lib/files');
const init = require('./lib/init');
const constr = require('./lib/constructfilestructure')

clear();

console.log(
  chalk.yellow(
    figlet.textSync('Scalaphy', { horizontalLayout: 'full' })
  )
);

const run = async () => {
    const stuff = await init.initProject()
    constr.initProject("aqq")
    console.log(stuff);
  };
  
run();