const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.aprobarDuenio = functions.https.onRequest(async (req, res) => {
    // LOG: Datos recibidos
    console.log("--- INICIO DE PROCESO DE APROBACIÓN ---");
    console.log("Query params recibidos:", req.query);

    const { uid, restId, restNombre, email } = req.query;

    if (!uid || !restId) {
        console.error("ERROR: Faltan UID o restId");
        return res.status(400).send("Faltan parámetros críticos");
    }

    try {
        const db = admin.firestore();

        // LOG: Paso 1
        console.log(`Intentando actualizar restaurante: ${restId} para el dueño: ${uid}`);
        
        // USO DE .doc() QUE ES EL CORRECTO PARA NODE.JS
        await db.collection('restaurantes').doc(restId).update({
            ownerId: uid
        });
        console.log("Restaurante actualizado con éxito");

        // LOG: Paso 2
        console.log(`Intentando actualizar usuario: ${uid}`);
        await db.collection('users').doc(uid).update({
            ownerOf: restId,
            isOwner: true
        });
        console.log("Usuario actualizado con éxito");

        res.send(`
            <div style="font-family:sans-serif; text-align:center; padding:50px;">
                <h1 style="color:#2E7D32;">✅ ¡HECHO!</h1>
                <p>El usuario <b>${email}</b> ya es dueño de <b>${restNombre}</b>.</p>
            </div>
        `);

    } catch (error) {
        // LOG DE ERROR CRÍTICO
        console.error("DETALLE DEL ERROR:", error);
        res.status(500).send("Error exacto en el servidor: " + error.message);
    }
});