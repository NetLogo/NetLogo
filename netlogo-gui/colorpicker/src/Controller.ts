import type { Express, Request, Response } from "express"

import express           from "express"
import path              from "path"
import { fileURLToPath } from "url"

const __dirname_ = path.dirname(fileURLToPath(import.meta.url))

const app: Express = express()

const serveDirAs = (dirName: string, urlName: string, fileExtension: string): void => {
  app.get(`/${urlName}/*.${fileExtension}`, (req: Request, res: Response) => {
    const relativePath = req.path.replace(new RegExp(`^\/${urlName}\/`), "")
    res.sendFile(path.resolve(`${__dirname_}/../../${dirName}/${relativePath}`))
  })
}

app.get("/", (_: Request, res: Response) => {
  res.sendFile(path.resolve(`${__dirname_}/../../html/index.html`))
})

serveDirAs(     "stylesheets", "stylesheets", "css")
serveDirAs("dist/typescripts", "javascripts",  "js")
serveDirAs(            "deps",        "deps",  "js")

const port     = 8007
const onLaunch = (): void => { console.log(`Server is running at https://localhost:${port}`) }
app.listen(port, onLaunch)
