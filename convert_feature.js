const sharp = require('sharp');
sharp('feature_graphic.svg')
  .resize(1024, 500)
  .png()
  .toFile('feature_graphic.png')
  .then(() => console.log('Successfully converted to feature_graphic.png'))
  .catch(err => console.error('Error:', err));
