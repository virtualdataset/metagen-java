var glob = require('glob');
var path = require('path');


export default {
  node: {
    fs: 'empty',
    dgram: 'empty'
  },
  mode: 'spa',
  /*
  ** Headers of the page
  */
  head: {
    title: process.env.npm_package_name || '',
    meta: [
      { charset: 'utf-8' },
      { name: 'viewport', content: 'width=device-width, initial-scale=1' },
      { hid: 'description', name: 'description', content: process.env.npm_package_description || '' }
    ],
    link: [
      { rel: 'icon', type: 'image/x-icon', href: '/favicon.ico' }
    ]
  },
  /*
  ** Customize the progress-bar color
  */
  loading: { color: '#fff' },
  /*
  ** Global CSS
  */
  css: [
  ],
  /*
  ** Plugins to load before mounting the App
  */
  plugins: [
  ],
  /*
  ** Nuxt.js dev-modules
  */
  buildModules: [
    // Simple usage
    '@nuxtjs/vuetify',
  ],
  /*
  ** Nuxt.js modules
  */
  modules: [
  ],

  /*
  ** Build configuration
  */
  build: {
    html: {
      minify: {
        minifyJS: false,
        minifyCSS: false
      }
    },
    optimization: {
      minimize: false
    }
  },
  vuetify: { 
    theme: {
      dark: true, 
      themes:{ 
        dark:{ 
          primary: '#FF7D2B',
          secondary: '#0C153A',
          accent: '#FF7D2B',
        }
      } 
    }
  },

  generate: {
    routes: dynamicRoutes
  }
}

var dynamicRoutes = getDynamicPaths({
 '/docs': 'docs/*.md'
});

/* https://github.com/jake-101/bael-template */
function getDynamicPaths(urlFilepathTable) {
  return [].concat(
    ...Object.keys(urlFilepathTable).map(url => {
      var filepathGlob = urlFilepathTable[url];
      return glob
        .sync(filepathGlob, { cwd: 'content' })
        .map(filepath => `${url}/${path.basename(filepath, '.md')}`);
    })
  );
}
