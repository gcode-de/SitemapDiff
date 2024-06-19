module.exports = {
    setupFilesAfterEnv: ['<rootDir>/src/setupTests.ts'],
    testEnvironment: 'jsdom',
    transform: {
        '^.+\\.tsx?$': 'ts-jest',
    },
    moduleNameMapper: {
        '\\.(jpg|jpeg|png|gif|webp|svg|png)$': '<rootDir>/src/__mocks__/fileMock.ts',
        '\\.(css|less|scss|sass)$': 'identity-obj-proxy',
    },
};
