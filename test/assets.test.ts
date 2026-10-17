import { describe, expect, it } from 'vitest'
import {
  fileName,
  scaledToFitScreen,
  spriteCount,
  SPRITES_PER_SCREEN,
} from '../src/assets'

describe('fileName', () => {
  it('takes the last segment of a path', () => {
    expect(fileName('./img/sprite/doge.png')).toBe('doge.png')
    expect(fileName('/assets/doge-scarf-a1b2c3.png')).toBe(
      'doge-scarf-a1b2c3.png',
    )
  })

  it('leaves a bare name alone', () => {
    expect(fileName('doge.png')).toBe('doge.png')
  })
})

describe('scaledToFitScreen', () => {
  it('scales a landscape image to the width when width runs out first', () => {
    expect(scaledToFitScreen(2000, 1000, 1000, 1000)).toEqual({
      width: 1000,
      height: 500,
      scaleFactor: 0.5,
    })
  })

  it('scales a landscape image to the height when height runs out first', () => {
    expect(scaledToFitScreen(2000, 1000, 4000, 800)).toEqual({
      width: 1600,
      height: 800,
      scaleFactor: 0.8,
    })
  })

  it('scales a portrait image up to fill a larger screen', () => {
    expect(scaledToFitScreen(500, 1000, 2000, 3000)).toEqual({
      width: 1500,
      height: 3000,
      scaleFactor: 3,
    })
  })
})

describe('spriteCount', () => {
  it('is the sprite density at a scale factor of one', () => {
    expect(spriteCount(1)).toBe(SPRITES_PER_SCREEN)
  })

  it('folds the size of the picture into the count', () => {
    expect(spriteCount(0.5)).toBe(SPRITES_PER_SCREEN / 2)
  })

  it('rounds a fractional count up, the way dotimes did', () => {
    expect(spriteCount(0.875)).toBe(263)
    expect(spriteCount(0)).toBe(0)
  })
})
