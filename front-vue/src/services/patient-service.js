export async function fetchPatients() {
    return [...patients]
}

export async function fetchPatientById(id) {
    return patients.find((p) => p.id === Number(id)) ?? null
}

export async function createPatient(newPatient) {
    const newId = Math.max(...patients.map((p) => p.id)) + 1
    const createdPatient = { ...newPatient, id: newId }
    patients.push(createdPatient)
    return createdPatient
}

export async function updatePatient(id, updatedPatient) {
    const index = patients.findIndex((p) => p.id === Number(id))
    if (index !== -1) {
        patients[index] = { ...updatedPatient, id: Number(id) }
        return patients[index]
    }
    return null
}

const patients = [
    {
        id: 1,
        firstName: 'TestNone',
        lastName: 'Test',
        birthDate: '1966-12-31',
        gender: 'F',
        address: '1 Brookside St',
        phone: '100-222-3333',
    },
    {
        id: 2,
        firstName: 'TestBorderline',
        lastName: 'Test',
        birthDate: '1945-06-24',
        gender: 'M',
        address: '2 High St',
        phone: '200-333-4444',
    },
    {
        id: 3,
        firstName: 'TestInDanger',
        lastName: 'Test',
        birthDate: '2004-06-18',
        gender: 'M',
        address: '3 Club Road',
        phone: '300-444-5555',
    },
    {
        id: 4,
        firstName: 'TestEarlyOnset',
        lastName: 'Test',
        birthDate: '2002-06-28',
        gender: 'F',
        address: '4 Valley Dr',
        phone: '400-555-6666',
    },
]
