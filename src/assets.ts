/**
 * Every image the app draws, plus the arithmetic that decides how big things
 * end up. The images are picked up from the directories at build time, so
 * dropping a new background or sprite into `src/img` is all it takes.
 */

/** The dog hidden in the picture, kept out of the scatter. */
export const TARGET_SPRITE = 'doge-scarf.png'

/** Sprites per unit of background scale factor. */
export const SPRITES_PER_SCREEN = 300

/** Sprite sizes as a fraction of the background scale factor. */
export const SPRITE_SCALE = 0.075
export const TARGET_SCALE = 0.2

/**
 * Backgrounds whose sprites want a size of their own, keyed by file name.
 * TODO: replace this with an image-specific scale factor.
 */
export const BACKGROUND_SPRITE_SCALE_FACTORS: Record<string, number> = {}

const spriteModules = import.meta.glob('./img/sprite/*', {
  eager: true,
  query: '?url',
  import: 'default',
}) as Record<string, string>

const backgroundModules = import.meta.glob('./img/bg/*', {
  eager: true,
  query: '?url',
  import: 'default',
}) as Record<string, string>

export const fileName = (path: string): string => path.split('/').pop() ?? path

const urlsByName = (modules: Record<string, string>): Record<string, string> =>
  Object.fromEntries(
    Object.entries(modules).map(([path, url]) => [fileName(path), url]),
  )

const spriteUrlsByName = urlsByName(spriteModules)

export const backgroundUrls: string[] = Object.values(
  urlsByName(backgroundModules),
)

export const targetSpriteUrl: string = spriteUrlsByName[TARGET_SPRITE]

export const otherSpriteUrls: string[] = Object.entries(spriteUrlsByName)
  .filter(([name]) => name !== TARGET_SPRITE)
  .map(([, url]) => url)

export const spriteScale = (
  backgroundScaleFactor: number,
  backgroundSpriteScaleFactor: number,
): number =>
  SPRITE_SCALE * backgroundScaleFactor * backgroundSpriteScaleFactor

export const targetScale = (
  backgroundScaleFactor: number,
  backgroundSpriteScaleFactor: number,
): number =>
  TARGET_SCALE * backgroundScaleFactor * backgroundSpriteScaleFactor

/** How many sprites to scatter over the background. Rounds up. */
export const spriteCount = (backgroundScaleFactor: number): number =>
  Math.ceil(SPRITES_PER_SCREEN * backgroundScaleFactor)

export interface FittedSize {
  width: number
  height: number
  scaleFactor: number
}

/** The largest size the image can be drawn at while still fitting the screen. */
export const scaledToFitScreen = (
  imageWidth: number,
  imageHeight: number,
  screenWidth: number,
  screenHeight: number,
): FittedSize => {
  const scaleFactor = Math.min(
    screenWidth / imageWidth,
    screenHeight / imageHeight,
  )
  return {
    width: imageWidth * scaleFactor,
    height: imageHeight * scaleFactor,
    scaleFactor,
  }
}
