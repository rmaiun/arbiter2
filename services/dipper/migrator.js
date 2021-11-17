const axios = require('axios').default
const { unzipSync } = require('zlib')
const StreamZip = require('node-stream-zip')
const { RabbitClient } = require('./app/rabbit-client')
const rc = new RabbitClient()
const zip = new StreamZip({
  file: 'C:\\hobby\\arbiter2\\services\\dipper\\dump.zip',
  storeEntries: true
})
const dotenv = require('dotenv')
dotenv.config()

zip.on('ready', async () => {
  // Take a look at the files
  console.log('Entries read: ' + zip.entriesCount)
  for (const entry of Object.values(zip.entries())) {
    const desc = entry.isDirectory ? 'directory' : `${entry.size} bytes`
    console.log(`Entry ${entry.name}: ${desc}`)
  }

  // Read a file in memory
  const seasonsData = zip.entryDataSync('seasons.json').toString('utf8')
  await processSeasons(seasonsData)

  const usersData = zip.entryDataSync('players.json').toString('utf8')
  await processPlayers(usersData)

  await processRounds(zip)

  // Do not forget to close the file once you're done
  zip.close()
  process.exit()
})

async function processSeasons (sd) {
  const seasons = JSON.parse(sd)
  console.log('The content of seasons.json is: ' + seasons)
  let sc = 1
  for (const s of seasons) {
    const data = {
      id: sc++,
      name: s.name,
      realm: 'ua_foosball',
    }
    if (s.seasonEndNotification) {
      data.endNotification = new Date(s.seasonEndNotification)
    }
    console.log(`creating season ${data.name}`)
    try {
      const response = await axios({
        url: 'http://localhost:9091/seasons/create',
        method: 'POST',
        headers:{"Arbiter2Auth":"test1"},
        data
      })
    } catch (e) {
      console.error(e)
    }
  }
}

async function processPlayers (ud) {
  const users = JSON.parse(ud)
  console.log('The content of players.json is: ' + users)
  const usersFiltered = users.filter(u => u.surname !== 'маюн')
  for (const u of usersFiltered) {
    const data = {
      user: {
        surname: u.surname
      },
      moderatorTid: 530809403,
    }
    if (u.tid) {
      data.user.tid = Number(u.tid)
    }
    try {
      console.log(`register user ${data.user.surname}`)
      await axios({
        url: 'http://localhost:9091/users/register',
        method: 'POST',
        headers:{"Arbiter2Auth":"test1"},
        data
      })
      console.log(`assign user to realm ${data.user.surname}`)
      await axios({
        url: 'http://localhost:9091/users/assignToRealm',
        method: 'POST',
        headers:{"Arbiter2Auth":"test1"},
        data: {
          user: u.surname,
          realm: 'ua_foosball',
          switchAsActive: true,
          moderatorTid: 530809403
        }
      })
    } catch (e) {
      console.error(e)
    }
  }
}

async function processRounds(zip) {
  const roundDataList = ['rounds_S1_2020.json', 'rounds_S3_2020.json', 'rounds_S4_2020.json', 'rounds_S1_2021.json', 'rounds_S2_2021.json', 'rounds_S3_2021.json', 'rounds_S4_2021.json']
  let c = 1
  let total = 0
  for (const rd of roundDataList) {
    const rStr = zip.entryDataSync(rd).toString('utf8')
    const roundList = JSON.parse(rStr)
    console.log(`${rd} contains ${roundList.length} items`)
    total += roundList.length
    for (const round of roundList) {
      const dto = {
        cmd: "addRound",
        chatId: 530809403,
        tid: 530809403,
        user: "migrator",
        data: {
          w1: round.winner1,
          w2: round.winner2,
          l1: round.loser1,
          l2: round.loser2,
          shutout: round.shutout,
          created: new Date(round.created),
          moderator: 530809403,
          season: round.season
        }
      }
      console.log(`publishing ${c++} ${dto.data.w1}/${dto.data.w2} - ${dto.data.l1}/${dto.data.l2} ${dto.data.shutout}`)
      await rc.publish(dto)
    }
  }
}
