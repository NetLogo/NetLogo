// This code is taken from Tortoise's 'colormodel.coffee' and should be replaced
// --Jason B. (6/11/25)

// type ColorNumber = Number
// type RGB         = (Number, Number, Number)

// Number
const ColorMax = 140;

// (Number, Number) => (Number) => Number
const attenuate = (lowerBound, upperBound) => (x) => {
  return (x < lowerBound) ? lowerBound :
        ((x > upperBound) ? upperBound : x);
};

// (Number) => Number
const attenuateRGB = attenuate(0, 255);

// (Number, Number, Number) => String
const componentsToKey = (r, g, b) => `${r}_${g}_${b}`;

// (String) => RGB
const keyToComponents = (key) =>
  key.split('_').map(parseFloat);

// Array[RGB]
const BaseRGBs = [
  [140, 140, 140], // gray       (5)
  [215,  48,  39], // red       (15)
  [241, 105,  19], // orange    (25)
  [156, 109,  70], // brown     (35)
  [237, 237,  47], // yellow    (45)
  [ 87, 176,  58], // green     (55)
  [ 42, 209,  57], // lime      (65)
  [ 27, 158, 119], // turquoise (75)
  [ 82, 196, 196], // cyan      (85)
  [ 43, 140, 190], // sky       (95)
  [ 50,  92, 168], // blue     (105)
  [123,  78, 163], // violet   (115)
  [166,  25, 105], // magenta  (125)
  [224, 126, 149], // pink     (135)
  [ 0,    0,   0], // black
  [255, 255, 255]  // white
]

// (Array[RGB], Object[RGB])
const [RGBCache, RGBMap] = (() => {

  const rgbMap = {};
  const cache  = [];

  for (let colorTimesTen = 0; colorTimesTen < ColorMax * 10; colorTimesTen++) {

    let initRGB = null;

    if (colorTimesTen === 0) {
      initRGB = [0, 0, 0];
    } else if (colorTimesTen === 99) {
      initRGB = [255, 255, 255];
    } else {

      const baseIndex = Math.floor(colorTimesTen / 100);
      const rgb       = BaseRGBs[baseIndex];
      const step      = (colorTimesTen % 100 - 50) / 50.48 + 0.012;
      const clamp     = (step <= 0) ? ((x) => x) : ((x) => 0xFF - x);

      initRGB = rgb.map((x) => x + Math.trunc(clamp(x) * step));

    }

    rgbMap[componentsToKey(...initRGB)] = colorTimesTen / 10;
    cache.push(initRGB)

  }

  return [cache, rgbMap];

})();

const colorToRGB = (num) => {
  const wrapColor = (x) => {
    const modColor = x % ColorMax
    return (modColor >= 0) ? modColor : ColorMax + modColor
  }
  return RGBCache[Math.floor(wrapColor(num) * 10)]
}

// (Number, Number, Number, Number, Number, Number) => Number
const colorDistance = (r1, g1, b1, r2, g2, b2) => {
  const rMean = r1 + Math.floor(r2 / 2);
  const rDiff = r1 - r2;
  const gDiff = g1 - g2;
  const bDiff = b1 - b2;
  return (((512 + rMean) * rDiff * rDiff) >> 8) +
           4 * gDiff * gDiff +
           (((767 - rMean) * bDiff * bDiff) >> 8);
};

// (RGB...) => ColorNumber
const estimateColorNumber = (r, g, b) => {
  const f =
    (acc, [k, v]) => {
      const [cr, cg, cb] = keyToComponents(k)
      const dist         = colorDistance(r, g, b, cr, cg, cb)
      return (dist < acc[1]) ? [v, dist] : acc;
    };
  return Object.entries(RGBMap).reduce(f, [0, Number.MAX_VALUE])[0];
};

// Object[ColorNumber]
const nearestColorMemo = {};

// (RGB...) => ColorNumber
const nearestColorNumberOfRGB = (r, g, b) => {

  const red   = attenuateRGB(r);
  const green = attenuateRGB(g);
  const blue  = attenuateRGB(b);

  const key  = componentsToKey(red, green, blue);
  const memo = nearestColorMemo[key];

  if (memo !== undefined) {
    return memo;
  } else {

    const colorNumber =
      (RGBMap[key] !== undefined) ? RGBMap[key] :
                                    estimateColorNumber(red, green, blue);

    nearestColorMemo[key] = colorNumber;

    return colorNumber;

  }

};

const NLColorModel =
  { colorToRGB
  , nearestColorNumberOfRGB
  };

window.NLColorModel = NLColorModel;
