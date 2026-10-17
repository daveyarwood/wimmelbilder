import './style.css'
import {
  BACKGROUND_SPRITE_SCALE_FACTORS,
  backgroundUrls,
  fileName,
  otherSpriteUrls,
  targetSpriteUrl,
  spriteCount,
  spriteScale,
  targetScale,
  scaledToFitScreen,
} from './assets'

const root = document.getElementById('root') as HTMLElement

const windowDimensions = (): [number, number] => {
  const width =
    window.innerWidth ||
    document.documentElement.clientWidth ||
    document.body.clientWidth
  const height =
    window.innerHeight ||
    document.documentElement.clientHeight ||
    document.body.clientHeight
  return [width, height]
}

const loadImage = (src: string): Promise<HTMLImageElement> => {
  return new Promise((resolve, reject) => {
    const image = new Image()
    image.addEventListener('load', () => resolve(image))
    image.addEventListener('error', () =>
      reject(new Error(`could not load ${src}`)),
    )
    image.src = src
  })
}

const replaceCanvas = (width: number, height: number): HTMLCanvasElement => {
  root.querySelector('canvas')?.remove()
  const canvas = document.createElement('canvas')
  canvas.width = width
  canvas.height = height
  root.appendChild(canvas)
  return canvas
}

export const randomCoordinate = (
  width: number,
  height: number,
): [number, number] => {
  return [Math.floor(Math.random() * width), Math.floor(Math.random() * height)]
}

const randomItem = <T>(items: T[]): T => {
  return items[Math.floor(Math.random() * items.length)]
}

const scatter = (
  ctx: CanvasRenderingContext2D,
  image: HTMLImageElement,
  scaleFactor: number,
): void => {
  const [x, y] = randomCoordinate(ctx.canvas.width, ctx.canvas.height)
  ctx.drawImage(
    image,
    x,
    y,
    image.width * scaleFactor,
    image.height * scaleFactor,
  )
}

export const render = async (): Promise<void> => {
  const backgroundUrl = randomItem(backgroundUrls)
  const backgroundSpriteScaleFactor =
    BACKGROUND_SPRITE_SCALE_FACTORS[fileName(backgroundUrl)] ?? 1

  const [background, target, otherSprites] = await Promise.all([
    loadImage(backgroundUrl),
    loadImage(targetSpriteUrl),
    Promise.all(otherSpriteUrls.map(loadImage)),
  ])

  const { width, height, scaleFactor } = scaledToFitScreen(
    background.width,
    background.height,
    ...windowDimensions(),
  )

  const canvas = replaceCanvas(width, height)
  const ctx = canvas.getContext('2d') as CanvasRenderingContext2D

  ctx.drawImage(background, 0, 0, width, height)

  const scatterScale = spriteScale(scaleFactor, backgroundSpriteScaleFactor)
  for (let i = 0; i < spriteCount(scaleFactor); i++) {
    scatter(ctx, randomItem(otherSprites), scatterScale)
  }

  scatter(ctx, target, targetScale(scaleFactor, backgroundSpriteScaleFactor))
}

export const init = (): void => {
  console.log('init')
  const renderWhenLoaded = () => void render()
  if (document.readyState === 'complete') {
    renderWhenLoaded()
  } else {
    window.addEventListener('load', renderWhenLoaded)
  }
}

init()

if (import.meta.hot) {
  import.meta.hot.accept(() => {
    console.log('reload')
    void render()
  })
}
