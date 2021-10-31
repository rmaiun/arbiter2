const { BotRunner } = require('./app/bot-runner')
const dotenv = require('dotenv')

console.log('BOT: Starting...')
dotenv.config()
console.log('BOT: Configs were loaded')

const br = new BotRunner(process.env.TOKEN)
delay(process.env.START_DELAY)
  .then(() => br.initBot())
  .then(bot => {
    process.once('SIGTERM', () => bot.stop('SIGTERM'))
    process.once('SIGINT', () => bot.stop('SIGINT'))
    console.log('BOT: Started successfully')
    return bot.launch()
  }).catch(err => {
    console.error(err)
    console.log('BOT: unexpected error')
    process.exit(1)
  })

function delay (ms) {
  console.log(`BOT: Delay for ${process.env.START_DELAY} sec`)
  return new Promise(resolve => setTimeout(resolve, ms))
}
