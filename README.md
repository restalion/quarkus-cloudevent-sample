# short-codes-data project

Se encargará de recoger los mensajes enviados tanto por AS400 como por la web de franquicias, para ello estará suscrito al evento *com.dia.store.product.shortcode.update*.

Este evento disparará la lógica de actualización de los datos en la base de datos dependiendo de si afecta a los códigos globales (no llevan informados el campo store) o los específicos de tienda. Después dispara un evento de tipo *com.dia.store.product.shortcode.invalidate* para indicar que es necesario invalidar la caché correspondiente al api.

Pone a disposición del proyecto api dos endpoints para recuperar tanto los códigos globales como los específicos de una tienda.