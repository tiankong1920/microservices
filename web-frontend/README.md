# Inventory Management System - Web Frontend

React-based frontend for the Inventory Management System, providing a modern dashboard for managing products, orders, inventory, sales, procurement, customers, and suppliers.

## Tech Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| React | 19 | UI framework |
| TypeScript | 5.9 | Type safety |
| Vite | 8 | Build tool / dev server |
| MUI (Material UI) | 7 | Component library |
| React Router | 7 | Client-side routing |
| Axios | 1.14 | HTTP client |
| ECharts | 6 | Charts & visualization |
| Recharts | 3 | Charts & visualization |
| Vitest | 4 | Unit testing |
| Testing Library | 16 | Component testing |
| ESLint | 9 | Linting |

## Getting Started

### Prerequisites

- Node.js 20+
- npm 10+

### Installation

```bash
npm install
```

### Available Scripts

| Command | Description |
|---------|-------------|
| `npm run dev` | Start development server with HMR |
| `npm run build` | Build for production |
| `npm run preview` | Preview production build |
| `npm run lint` | Run ESLint |
| `npm run test` | Run tests (Vitest) |
| `npm run test:watch` | Run tests in watch mode |
| `npm run test:coverage` | Run tests with coverage report |

## Project Structure

```
web-frontend/
├── public/                    # Static assets
├── src/
│   ├── assets/                # Images, fonts, etc.
│   ├── components/            # Shared UI components
│   ├── contexts/              # React Context providers
│   ├── pages/                 # Page components
│   │   ├── Dashboard/         # Dashboard pages
│   │   ├── Products/          # Product management
│   │   ├── Orders/            # Order management
│   │   ├── Inventory/         # Inventory management
│   │   ├── Sales/             # Sales management
│   │   ├── Procurement/       # Procurement management
│   │   └── ...
│   ├── services/              # API client & service layer
│   ├── test/                  # Test setup utilities
│   ├── types/                 # TypeScript type definitions
│   ├── App.tsx                # Root component with routing
│   └── main.tsx               # Application entry point
├── eslint.config.js           # ESLint configuration
├── vite.config.js             # Vite configuration
├── vitest.config.ts           # Vitest configuration
├── tsconfig.json              # TypeScript configuration
└── package.json               # Dependencies & scripts
```

## Environment Setup

The frontend connects to the backend API gateway at `http://localhost:8080` by default. To change this, set the `VITE_API_BASE_URL` environment variable:

```bash
# Windows
set VITE_API_BASE_URL=http://localhost:8080

# PowerShell
$env:VITE_API_BASE_URL="http://localhost:8080"
```

## Development

The dev server runs on `http://localhost:5173` with HMR enabled. API requests are proxied to the backend services.

### Code Style

- ESLint with TypeScript rules for code quality
- Prefer functional components with hooks
- Use MUI's `sx` prop or styled components for styling
- Follow existing patterns for API calls (services layer)

## Build

```bash
npm run build
```

Output is written to `dist/`. The build assumes the frontend is served from the root path. For custom base paths, update the `base` option in `vite.config.js`.

## Docker

```bash
# Build image
docker build -t inventory-frontend .

# Run container
docker run -p 80:80 inventory-frontend
```

The Dockerfile uses nginx to serve the production build.
