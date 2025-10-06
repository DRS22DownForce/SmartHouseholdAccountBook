import { type ExpenseFormData } from "./ExpenseFormData";

export interface ExpenseFormProps {
    initialData: Partial<ExpenseFormData>;
    error?: string | null;
    onChange: (data: ExpenseFormData) => void;
}