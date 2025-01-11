package com.cgvsu.HistoryBuffer;

public class ActionHistory<T extends Action> {
    private static class ActionNode<T> {
        T  action;            // Экшен, который выполняется
        ActionNode<T> prev;  // Ссылка на предыдущий узел
        ActionNode<T> next;  // Ссылка на следующий узел

        ActionNode(T action) {
            this.action = action;
        }
    }

    private ActionNode<T> head;    // Голова списка (самое новое действие)
    private ActionNode<T> tail;    // Хвост списка (самое старое действие)
    private ActionNode<T> current; // Текущая позиция для Undo/Redo
    private final int maxDepth;    // Максимальная глубина истории
    private int size;              // Текущее количество узлов

    public ActionHistory(int maxDepth) {
        this.maxDepth = maxDepth;
        this.size = 0;
    }

    // Добавление действия в начало (голова)
    // Добавление действия справа от текущего элемента
    public void addAction(T action) {
        ActionNode<T> newNode = new ActionNode<>(action);

        if (head == null) {
            // Если список пустой, инициализируем его
            head = newNode;
            tail = newNode;
            current = head;
        } else {
            if (current == null) {
                // Если текущий элемент null, добавляем в начало
                newNode.next = head;
                head.prev = newNode;
                head = newNode;
                current = head;
            } else {
                // Если текущий элемент существует
                if (current.next != null) {
                    // Обрезаем все элементы после текущего
                    current.next.prev = null;
                    ActionNode<T> temp = current.next;
                    while (temp != null) {
                        ActionNode<T> nextTemp = temp.next;
                        temp.next = null;
                        temp.prev = null;
                        temp = nextTemp;
                    }
                }

                // Вставляем новый узел
                newNode.prev = current;
                newNode.next = current.next;
                current.next = newNode;
                if (newNode.next == null) {
                    // Если новый элемент стал последним, обновляем tail
                    tail = newNode;
                }
                current = newNode; // Новый элемент становится текущим
            }
        }

        // Увеличиваем размер и проверяем на превышение maxDepth
        size++;
        if (size > maxDepth && tail != null) { // Проверяем, что tail не равен null
            tail = tail.prev; // Сдвигаем tail
            if (tail != null) {
                tail.next = null; // Отрываем хвост
            }
            size--;
        }
    }



    // Undo действие
    public void undo() {
        if (current != null) {
            System.out.println("undo" + current);
            current.action.undo(); // Отменяем действие
            current = current.next; // Переходим к следующему
        }
    }

    // Redo действие
    public void redo() {
        if (current != null && current.prev != null) {
            System.out.println("redo" + current);
            current = current.prev; // Возвращаемся к предыдущему
            current.action.redo(); // Повторяем действие
        }
    }

    // Удаление некорректного действия
    public void removeInvalid(ActionNode<T> node) {
        if (node == null) return;

        if (node == head) {
            head = head.next;
            if (head != null) head.prev = null;
        } else if (node == tail) {
            tail = tail.prev;
            if (tail != null) tail.next = null;
        } else {
            node.prev.next = node.next;
            node.next.prev = node.prev;
        }
        size--;
    }
}
