# Guía para subir CrediYa a GitHub

## 1. Qué SÍ se sube

- `src/` (todo el código fuente, incluidas las pruebas)
- `pom.xml`
- `sql/crediya_db.sql`
- `config.properties.example` (plantilla **sin** contraseña real)
- `README.md` y la carpeta `docs/`
- `.gitignore`

## 2. Qué NO se sube

| Archivo / carpeta | Por qué |
|---|---|
| `config.properties` | Contiene tu contraseña de MySQL. |
| `data/` | Archivos .txt con datos de clientes y empleados (datos personales). |
| `target/`, `out/` | Archivos compilados; se regeneran. |
| `.idea/`, `*.iml` | Configuración personal de IntelliJ. |

Todo esto ya está en el archivo `.gitignore` incluido en el proyecto.

## 3. Cómo se manejan las credenciales

La contraseña **nunca** está escrita en el código. El programa la lee de `config.properties`, un archivo local
que Git ignora. En GitHub solo queda `config.properties.example`, que tiene un texto de relleno.

Quien clone el proyecto copia `config.properties.example` como `config.properties` y escribe su propia contraseña.

## 4. Antes del primer commit: comprobar que no se filtra nada

En la terminal de IntelliJ (pestaña **Terminal**, abajo), dentro de la carpeta del proyecto:

```
git init
git status
```

En la lista de archivos **no deben aparecer** `config.properties` ni la carpeta `data/`.
Si aparecen, revisa que `.gitignore` esté en la raíz del proyecto y escrito tal cual.

## 5. Primer commit

```
git add .
git status
git commit -m "Proyecto CrediYa: sistema de préstamos en consola con archivos y MySQL"
```

(`git status` antes del commit es para revisar una vez más qué se va a subir.)

Si Git te dice que no sabe quién eres, configúralo una sola vez con tus propios datos:

```
git config --global user.name "TU NOMBRE"
git config --global user.email "TU CORREO"
```

## 6. Crear el repositorio y hacer push

1. En github.com: **New repository**. Ponle un nombre (por ejemplo `proyecto-crediyaJA`).
   **No** marques "Add a README" ni ".gitignore" (ya los tienes). Puede ser público o privado.
2. GitHub te muestra la URL del repositorio. Luego, en la terminal del proyecto:

```
git branch -M main
git remote add origin URL_DE_TU_REPOSITORIO
git push -u origin main
```

La primera vez, GitHub te pedirá autenticarte (ventana del navegador o un *token*). Eso lo haces tú; yo nunca necesito
tus credenciales de GitHub.

## 7. Si por error subiste una contraseña

Cambiar la contraseña de MySQL de inmediato es lo primero: borrar el archivo después **no** la elimina del historial de Git.
Después, quita el archivo del control de versiones con `git rm --cached config.properties` y haz otro commit.

## 8. Commits siguientes

```
git add .
git commit -m "Describe aquí qué cambiaste"
git push
```
