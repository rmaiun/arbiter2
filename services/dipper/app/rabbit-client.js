'use strict'
const amqp = require('amqplib')
const inputChannel = 'input_q'
const outputChannel = 'output_q'

class RabbitClient {
  async _getConnection () {
    if (!this._conn) {
      console.log(`Asking host ${process.env.RABBITMQ_URI} for connection`)
      this._conn = await amqp.connect(process.env.RABBITMQ_URI)
    }
    return this._conn
  }

  async _getProdChannel () {
    if (!this._prodChannel) {
      const conn = await this._getConnection()
      this._prodChannel = await conn.createChannel()
    }
    return this._prodChannel
  }

  async publish (data) {
    const prodChannel = await this._getProdChannel()
    // await prodChannel.assertExchange("bot_exchange", "Topic")
    return prodChannel.publish('bot_exchange', 'bot_in_rk', Buffer.from(JSON.stringify(data)), null)
  }

  async initConsumer (bot) {
    const connection = await this._getConnection()
    const consChannel = await connection.createChannel()
    await consChannel.assertQueue(outputChannel, { durable: true })
    await consChannel.consume(outputChannel, async function (msg) {
      if (msg !== null) {
        const data = JSON.parse(msg.content)
        try {
          await bot.telegram.sendMessage(data.chatId, data.result,
            {
              parse_mode: 'Markdown',
              reply_markup: JSON.stringify({
                keyboard: [
                  [{ text: 'Season Stats \uD83D\uDCC8' }, { text: 'Elo Rating \uD83D\uDDFF' }]
                ],
                resize_keyboard: true
              })
            })
          console.log('ok')
          consChannel.ack(msg)
        } catch (e) {
          console.log('nok')
          console.error(e)
          consChannel.nack(msg)
        }
      }
    })
  }
}

module.exports = { RabbitClient }
