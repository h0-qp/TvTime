const sharp = require('sharp');
sharp('icon.svg')
  .resize(512, 512)
  .png()
  .toFile('aptoide_icon_512.png')
  .then(() => console.log('Successfully converted to aptoide_icon_512.png'))
  .catch(err => console.error('Error:', err));
